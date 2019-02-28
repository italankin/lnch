package com.italankin.lnch.model.repository.descriptor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
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

import static com.italankin.lnch.model.repository.descriptor.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.LauncherActivityInfoUtils.getDominantIconColor;
import static com.italankin.lnch.model.repository.descriptor.LauncherActivityInfoUtils.getLabel;
import static com.italankin.lnch.model.repository.descriptor.LauncherActivityInfoUtils.getVersionCode;

public class LauncherDescriptorRepository implements DescriptorRepository {
    private final PackageManager packageManager;
    private final DescriptorStore descriptorStore;
    private final PackagesStore packagesStore;
    private final ShortcutsRepository shortcutsRepository;
    private final LauncherApps launcherApps;
    private final Preferences preferences;

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
                .map(appsData -> {
                    switch (preferences.get(Preferences.APPS_SORT_MODE)) {
                        case AZ: {
                            boolean changed = new AscLabelSorter().sort(appsData.items);
                            return new AppsData(appsData.items, changed || appsData.changed);
                        }
                        case ZA: {
                            boolean changed = new DescLabelSorter().sort(appsData.items);
                            return new AppsData(appsData.items, changed || appsData.changed);
                        }
                        case MANUAL:
                        default:
                            return appsData;
                    }
                })
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
                    for (int i = 0, s = infoList.size(); i < s; i++) {
                        items.add(createItem(infoList.get(i)));
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
                    List<Descriptor> items = new ArrayList<>(savedItems.size());
                    List<Descriptor> deleted = new ArrayList<>(8);
                    Map<String, List<LauncherActivityInfo>> infosByPackageName = infosByPackageName(infoList);
                    List<Shortcut> pinnedShortcuts = shortcutsRepository.getPinnedShortcuts();
                    Map<String, AppDescriptor> installedApps = new HashMap<>(savedItems.size());
                    for (Descriptor item : savedItems) {
                        if (item instanceof PinnedShortcutDescriptor) {
                            String uri = ((PinnedShortcutDescriptor) item).uri;
                            Intent intent = IntentUtils.fromUri(uri);
                            if (IntentUtils.canHandleIntent(packageManager, intent)) {
                                items.add(item);
                            } else {
                                deleted.add(item);
                            }
                            continue;
                        }
                        if (item instanceof DeepShortcutDescriptor) {
                            items.add(item);
                            DeepShortcutDescriptor descriptor = (DeepShortcutDescriptor) item;
                            if (pinnedShortcuts.isEmpty()) {
                                descriptor.enabled = false;
                                continue;
                            }
                            boolean enabled = false;
                            for (Iterator<Shortcut> iter = pinnedShortcuts.iterator(); iter.hasNext(); ) {
                                Shortcut pinned = iter.next();
                                if (pinned.getPackageName().equals(descriptor.packageName)
                                        && pinned.getId().equals(descriptor.id)) {
                                    iter.remove();
                                    enabled = pinned.isEnabled();
                                    break;
                                }
                            }
                            descriptor.enabled = enabled;
                            continue;
                        }
                        if (!(item instanceof AppDescriptor)) {
                            items.add(item);
                            continue;
                        }
                        AppDescriptor app = (AppDescriptor) item;
                        LauncherActivityInfo info = findInfo(infosByPackageName, app);
                        if (info != null) {
                            long versionCode = getVersionCode(packageManager, app.packageName);
                            if (app.versionCode != versionCode) {
                                app.versionCode = versionCode;
                                app.label = getLabel(info);
                                app.color = getDominantIconColor(info,
                                        preferences.get(Preferences.COLOR_THEME) == Preferences.ColorTheme.DARK);
                            }
                            if (app.componentName != null) {
                                app.componentName = getComponentName(info);
                            }
                            items.add(app);
                            installedApps.put(app.packageName, app);
                        } else {
                            deleted.add(app);
                        }
                    }
                    for (List<LauncherActivityInfo> infos : infosByPackageName.values()) {
                        if (infos.size() == 1) {
                            AppDescriptor item = createItem(infos.get(0));
                            items.add(item);
                            installedApps.put(item.packageName, item);
                        } else {
                            for (LauncherActivityInfo info : infos) {
                                AppDescriptor item = createItem(info);
                                item.componentName = getComponentName(info);
                                items.add(item);
                                installedApps.put(item.packageName, item);
                            }
                        }
                    }
                    for (Shortcut shortcut : pinnedShortcuts) {
                        String packageName = shortcut.getPackageName();
                        DeepShortcutDescriptor item = new DeepShortcutDescriptor(
                                packageName, shortcut.getId());
                        AppDescriptor app = installedApps.get(packageName);
                        item.color = app.color;
                        String label = shortcut.getShortLabel().toString();
                        if (TextUtils.isEmpty(label)) {
                            item.label = app.getVisibleLabel();
                        } else {
                            item.label = label.toUpperCase(Locale.getDefault());
                        }
                        items.add(item);
                    }
                    boolean changed = !deleted.isEmpty() || !infosByPackageName.isEmpty()
                            || !pinnedShortcuts.isEmpty();
                    emitter.onSuccess(new AppsData(items, changed));
                });
    }

    private Map<String, List<LauncherActivityInfo>> infosByPackageName(List<LauncherActivityInfo> infoList) {
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

    private LauncherActivityInfo findInfo(Map<String, List<LauncherActivityInfo>> map, AppDescriptor item) {
        List<LauncherActivityInfo> infos = map.get(item.packageName);
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        if (infos.size() == 1) {
            LauncherActivityInfo result = infos.remove(0);
            map.remove(item.packageName);
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

    private AppDescriptor createItem(LauncherActivityInfo info) {
        String packageName = info.getApplicationInfo().packageName;
        AppDescriptor item = new AppDescriptor(packageName);
        item.versionCode = getVersionCode(packageManager, packageName);
        item.label = getLabel(info);
        item.color = getDominantIconColor(info,
                preferences.get(Preferences.COLOR_THEME) == Preferences.ColorTheme.DARK);
        return item;
    }

    private void writeToDisk(List<Descriptor> items) {
        descriptorStore.write(packagesStore.output(), items);
    }

    private static class AppsData {
        final List<Descriptor> items;
        final boolean changed;

        AppsData(List<Descriptor> items, boolean changed) {
            this.items = items;
            this.changed = changed;
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
