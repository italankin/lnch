package com.italankin.lnch.model.repository.descriptor.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.sort.AscLabelSorter;
import com.italankin.lnch.model.repository.descriptor.sort.DescLabelSorter;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.IntentUtils;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import static com.italankin.lnch.model.repository.descriptor.apps.LauncherActivityInfoUtils.getComponentName;

public class LauncherDescriptorRepository implements DescriptorRepository {
    private final PackageManager packageManager;
    private final DescriptorStore descriptorStore;
    private final PackagesStore packagesStore;
    private final ShortcutsRepository shortcutsRepository;
    private final LauncherApps launcherApps;
    private final Preferences preferences;
    private final AppDescriptors appDescriptors;

    private final Completable updater;
    private final BehaviorSubject<List<Descriptor>> updatesSubject = BehaviorSubject.create();

    public LauncherDescriptorRepository(Context context, PackageManager packageManager,
            DescriptorStore descriptorStore, PackagesStore packagesStore,
            ShortcutsRepository shortcutsRepository, Preferences preferences) {
        this.packageManager = packageManager;
        this.descriptorStore = descriptorStore;
        this.packagesStore = packagesStore;
        this.shortcutsRepository = shortcutsRepository;
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.preferences = preferences;
        this.appDescriptors = new AppDescriptors(packageManager, preferences);
        this.updater = createUpdater();
        subscribeForUpdates();
    }

    @Override
    public Completable update() {
        return updater;
    }

    @Override
    public Observable<List<Descriptor>> observe() {
        return updatesSubject;
    }

    @Override
    public List<Descriptor> items() {
        List<Descriptor> value = updatesSubject.getValue();
        return value != null ? value : Collections.emptyList();
    }

    @Override
    public <T extends Descriptor> List<T> itemsOfType(Class<T> klass) {
        List<Descriptor> items = items();
        List<T> result = new ArrayList<>(items.size());
        for (Descriptor item : items) {
            if (klass.isAssignableFrom(item.getClass())) {
                result.add(klass.cast(item));
            }
        }
        return result;
    }

    @Override
    public DescriptorRepository.Editor edit() {
        return new Editor(updatesSubject.getValue());
    }

    @Override
    public Completable clear() {
        return Completable.fromRunnable(packagesStore::clear)
                .andThen(updater);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void subscribeForUpdates() {
        new LauncherAppsUpdates(launcherApps)
                .flatMapCompletable(change -> updater.onErrorComplete())
                .onErrorComplete(throwable -> {
                    Timber.e(throwable, "subscribeForUpdates");
                    return true;
                })
                .subscribe();

        preferences.observe(Preferences.APPS_SORT_MODE)
                .filter(mode -> mode != Preferences.AppsSortMode.MANUAL)
                .flatMapCompletable(s -> update())
                .subscribe();
    }

    private Completable createUpdater() {
        return loadAll()
                .map(this::applySorting)
                .map(this::applyOverlayColor)
                .doOnSuccess(appsData -> {
                    if (appsData.changed) {
                        Timber.d("data has changed, write to disk");
                        writeToDisk(appsData.items);
                    }
                })
                .map(appsData -> Collections.unmodifiableList(appsData.items))
                .doOnSuccess(updatesSubject::onNext)
                .doOnError(e -> Timber.e(e, "updater:"))
                .ignoreElement();
    }

    private AppsData applySorting(AppsData appsData) {
        switch (preferences.get(Preferences.APPS_SORT_MODE)) {
            case AZ: {
                boolean changed = new AscLabelSorter().sort(appsData.items);
                return appsData.copy(changed);
            }
            case ZA: {
                boolean changed = new DescLabelSorter().sort(appsData.items);
                return appsData.copy(changed);
            }
            case MANUAL:
            default:
                return appsData;
        }
    }

    private AppsData applyOverlayColor(AppsData appsData) {
        if (preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW)) {
            Integer colorOverlay = preferences.get(Preferences.APPS_COLOR_OVERLAY);
            for (Descriptor item : appsData.items) {
                if (item instanceof GroupDescriptor) {
                    continue;
                }
                if (item instanceof CustomColorDescriptor) {
                    ((CustomColorDescriptor) item).setCustomColor(colorOverlay);
                }
            }
        }
        return appsData;
    }

    private Single<AppsData> loadAll() {
        return Single
                .fromCallable(() -> launcherApps.getActivityList(null, Process.myUserHandle()))
                .flatMap(infoList -> {
                    Single<AppsData> fromList = loadFromList(infoList);
                    return loadFromFile(infoList)
                            .switchIfEmpty(fromList)
                            .doOnError(throwable -> Timber.e(throwable, "loadAll:"))
                            .onErrorResumeNext(fromList);
                });
    }

    private Single<AppsData> loadFromList(List<LauncherActivityInfo> infoList) {
        return Single
                .fromCallable(() -> {
                    List<Descriptor> items = new ArrayList<>(16);
                    for (LauncherActivityInfo info : infoList) {
                        items.add(appDescriptors.createItem(info));
                    }
                    return new AppsData(items, true);
                });
    }

    private Maybe<AppsData> loadFromFile(List<LauncherActivityInfo> infoList) {
        return Maybe
                .create(emitter -> {
                    InputStream packagesInput = packagesStore.input();
                    if (packagesInput == null) {
                        emitter.onComplete();
                        return;
                    }
                    List<Descriptor> savedItems = descriptorStore.read(packagesInput);
                    if (savedItems == null) {
                        emitter.onComplete();
                        return;
                    }
                    ProcessingEnv env = new ProcessingEnv(shortcutsRepository.getPinnedShortcuts(), infoList);
                    for (Descriptor item : savedItems) {
                        if (item instanceof PinnedShortcutDescriptor) {
                            visitPinnedShortcut(env, (PinnedShortcutDescriptor) item);
                        } else if (item instanceof DeepShortcutDescriptor) {
                            visitDeepShortcut(env, (DeepShortcutDescriptor) item);
                        } else if (item instanceof AppDescriptor) {
                            visitApp(env, (AppDescriptor) item);
                        } else {
                            env.items.add(item);
                        }
                    }
                    processNewApps(env);
                    processShortcuts(env);
                    emitter.onSuccess(env.getData());
                });
    }

    private void processShortcuts(ProcessingEnv env) {
        for (Shortcut shortcut : env.shortcuts) {
            String packageName = shortcut.getPackageName();
            DeepShortcutDescriptor item = new DeepShortcutDescriptor(
                    packageName, shortcut.getId());
            AppDescriptor app = env.installed.get(packageName);
            assert app != null;
            item.color = app.color;
            String label = shortcut.getShortLabel().toString();
            if (TextUtils.isEmpty(label)) {
                item.label = app.getVisibleLabel();
            } else {
                item.label = label.toUpperCase(Locale.getDefault());
            }
            env.items.add(item);
        }
    }

    private void processNewApps(ProcessingEnv env) {
        for (List<LauncherActivityInfo> infos : env.packagesMap.lists()) {
            if (infos.size() == 1) {
                AppDescriptor item = appDescriptors.createItem(infos.get(0));
                env.items.add(item);
                env.installed.put(item.packageName, item);
            } else {
                for (LauncherActivityInfo info : infos) {
                    AppDescriptor item = appDescriptors.createItem(info, getComponentName(info));
                    env.items.add(item);
                    env.installed.put(item.packageName, item);
                }
            }
        }
    }

    private void visitApp(ProcessingEnv env, AppDescriptor app) {
        LauncherActivityInfo info = env.packagesMap.poll(app);
        if (info != null) {
            appDescriptors.updateItem(app, info);
            env.items.add(app);
            env.installed.put(app.packageName, app);
        } else {
            env.deleted.add(app);
        }
    }

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor item) {
        env.items.add(item);
        if (env.shortcuts.isEmpty()) {
            item.enabled = false;
            return;
        }
        for (Iterator<Shortcut> iter = env.shortcuts.iterator(); iter.hasNext(); ) {
            Shortcut pinned = iter.next();
            if (pinned.getPackageName().equals(item.packageName)
                    && pinned.getId().equals(item.id)) {
                iter.remove();
                item.enabled = pinned.isEnabled();
                return;
            }
        }
    }

    private void visitPinnedShortcut(ProcessingEnv env, PinnedShortcutDescriptor item) {
        String uri = item.uri;
        Intent intent = IntentUtils.fromUri(uri);
        if (IntentUtils.canHandleIntent(packageManager, intent)) {
            env.items.add(item);
        } else {
            env.deleted.add(item);
        }
    }

    private void writeToDisk(List<Descriptor> items) {
        descriptorStore.write(packagesStore.output(), items);
    }

    private static class ProcessingEnv {
        final List<Descriptor> items = new ArrayList<>(64);
        final List<Descriptor> deleted = new ArrayList<>(8);
        final PackagesMap packagesMap;
        final List<Shortcut> shortcuts;
        final Map<String, AppDescriptor> installed = new HashMap<>(64);

        ProcessingEnv(List<Shortcut> shortcuts, List<LauncherActivityInfo> infoList) {
            this.shortcuts = shortcuts;
            this.packagesMap = new PackagesMap(infoList);
        }

        AppsData getData() {
            boolean changed = !deleted.isEmpty() || !packagesMap.isEmpty()
                    || !shortcuts.isEmpty();
            return new AppsData(items, changed);
        }
    }

    private static class PackagesMap {
        private final Map<String, List<LauncherActivityInfo>> packages;

        PackagesMap(List<LauncherActivityInfo> infoList) {
            this.packages = groupByPackage(infoList);
        }

        boolean isEmpty() {
            return packages.isEmpty();
        }

        Collection<List<LauncherActivityInfo>> lists() {
            return packages.values();
        }

        private LauncherActivityInfo poll(AppDescriptor item) {
            List<LauncherActivityInfo> infos = packages.get(item.packageName);
            if (infos == null || infos.isEmpty()) {
                return null;
            }
            if (infos.size() == 1) {
                LauncherActivityInfo result = infos.remove(0);
                packages.remove(item.packageName);
                return result;
            } else if (item.componentName != null) {
                Iterator<LauncherActivityInfo> iter = infos.iterator();
                while (iter.hasNext()) {
                    LauncherActivityInfo info = iter.next();
                    String componentName = getComponentName(info);
                    if (componentName.equals(item.componentName)) {
                        iter.remove();
                        return info;
                    }
                }
            }
            return null;
        }

        private static Map<String, List<LauncherActivityInfo>> groupByPackage(List<LauncherActivityInfo> infoList) {
            Map<String, List<LauncherActivityInfo>> infosByPackageName = new HashMap<>(infoList.size());
            for (LauncherActivityInfo info : infoList) {
                String packageName = info.getApplicationInfo().packageName;
                List<LauncherActivityInfo> list = infosByPackageName.get(packageName);
                if (list == null) {
                    list = new ArrayList<>(1);
                    infosByPackageName.put(packageName, list);
                }
                list.add(info);
            }
            return infosByPackageName;
        }
    }

    private static class AppsData {
        final List<Descriptor> items;
        final boolean changed;

        AppsData(List<Descriptor> items, boolean changed) {
            this.items = items;
            this.changed = changed;
        }

        AppsData copy(boolean changed) {
            return new AppsData(items, changed || this.changed);
        }
    }

    final class Editor implements DescriptorRepository.Editor {
        private final Queue<DescriptorRepository.Editor.Action> actions = new ArrayDeque<>();
        private final List<Descriptor> items;
        private volatile boolean used;

        Editor(List<Descriptor> items) {
            this.items = items;
        }

        @Override
        public Editor enqueue(DescriptorRepository.Editor.Action action) {
            if (used) {
                throw new IllegalStateException();
            }
            actions.offer(action);
            return this;
        }

        @Override
        public boolean isEmpty() {
            return actions.isEmpty();
        }

        @Override
        public DescriptorRepository.Editor clear() {
            actions.clear();
            return this;
        }

        @Override
        public Completable commit() {
            if (used) {
                throw new IllegalStateException();
            }
            Consumer<Disposable> onSubscribe = d -> used = true;
            if (actions.isEmpty()) {
                Timber.d("commit: no actions");
                return Completable.complete()
                        .doOnSubscribe(onSubscribe);
            }
            Timber.d("commit: apply actions");
            return Single
                    .fromCallable(() -> {
                        List<Descriptor> result = new ArrayList<>(items);
                        Iterator<DescriptorRepository.Editor.Action> iter = actions.iterator();
                        while (iter.hasNext()) {
                            iter.next().apply(result);
                            iter.remove();
                        }
                        return result;
                    })
                    .doOnSubscribe(onSubscribe)
                    .doOnSuccess(LauncherDescriptorRepository.this::writeToDisk)
                    .flatMapCompletable(descriptors -> updater);
        }
    }
}
