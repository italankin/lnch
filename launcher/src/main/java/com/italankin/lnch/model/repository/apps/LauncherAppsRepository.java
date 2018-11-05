package com.italankin.lnch.model.repository.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.IntentUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

import static com.italankin.lnch.model.repository.apps.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.apps.LauncherActivityInfoUtils.getDominantIconColor;
import static com.italankin.lnch.model.repository.apps.LauncherActivityInfoUtils.getLabel;
import static com.italankin.lnch.model.repository.apps.LauncherActivityInfoUtils.getVersionCode;

public class LauncherAppsRepository implements AppsRepository {
    private final Context context;
    private final PackageManager packageManager;
    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final LauncherApps launcherApps;
    private final Completable updater;
    private final BehaviorSubject<List<Descriptor>> updatesSubject = BehaviorSubject.create();
    private final Subject<String> packageChangesSubject = PublishSubject.create();
    private final CompositeDisposable disposeBag = new CompositeDisposable();

    public LauncherAppsRepository(Context context, PackageManager packageManager,
            DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository) {
        this.context = context;
        this.packageManager = packageManager;
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        updater = loadAll()
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
        packageChangesSubject
                .doOnNext(Timber::d)
                .debounce(1, TimeUnit.SECONDS)
                .flatMapCompletable(change -> updater.onErrorComplete())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposeBag.add(d);
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "change:");
                    }
                });
        //noinspection ConstantConditions
        launcherApps.registerCallback(new LauncherCallbacks());
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
    public Descriptor findDescriptor(String id) {
        List<Descriptor> items = items();
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public AppsRepository.Editor edit() {
        return new Editor(updatesSubject.getValue());
    }

    @Override
    public Completable clear() {
        return Completable.fromCallable(() -> getPackagesFile().delete())
                .andThen(updater);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

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
                    if (!getPackagesFile().exists()) {
                        emitter.onComplete();
                        return;
                    }
                    List<Descriptor> savedItems = descriptorRepository.read(getPackagesFile());
                    if (savedItems != null) {
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
                                DeepShortcutDescriptor descriptor = (DeepShortcutDescriptor) item;
                                if (pinnedShortcuts.isEmpty()) {
                                    deleted.add(descriptor);
                                    continue;
                                }
                                boolean found = false;
                                for (Iterator<Shortcut> iter = pinnedShortcuts.iterator(); iter.hasNext(); ) {
                                    Shortcut pinned = iter.next();
                                    if (pinned.getPackageName().equals(descriptor.packageName)
                                            && pinned.getId().equals(descriptor.id)) {
                                        iter.remove();
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    items.add(descriptor);
                                } else {
                                    deleted.add(descriptor);
                                }
                                continue;
                            }
                            if (!(item instanceof AppDescriptor)) {
                                items.add(item);
                                continue;
                            }
                            AppDescriptor app = (AppDescriptor) item;
                            LauncherActivityInfo info = findInfo(infosByPackageName, app);
                            if (info != null) {
                                int versionCode = getVersionCode(packageManager, app.packageName);
                                if (app.versionCode != versionCode) {
                                    app.versionCode = versionCode;
                                    app.label = getLabel(info);
                                    app.color = getDominantIconColor(info);
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
                                items.add(createItem(infos.get(0)));
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
                    }
                    emitter.onComplete();
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
        item.color = getDominantIconColor(info);
        return item;
    }

    private void writeToDisk(List<Descriptor> items) {
        descriptorRepository.write(getPackagesFile(), items);
    }

    private File getPackagesFile() {
        return new File(context.getFilesDir(), "packages.json");
    }

    private static class AppsData {
        final List<Descriptor> items;
        final boolean changed;

        public AppsData(List<Descriptor> items, boolean changed) {
            this.items = items;
            this.changed = changed;
        }
    }

    final class LauncherCallbacks extends LauncherApps.Callback {
        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            notify(user, "package removed");
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            notify(user, "package added");
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            notify(user, "package changed");
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            notify(user, "packages available");
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            notify(user, "packages unavailable");
        }

        private void notify(UserHandle user, String s) {
            if (Process.myUserHandle().equals(user)) {
                packageChangesSubject.onNext(s);
            }
        }
    }

    final class Editor implements AppsRepository.Editor {
        private final Queue<AppsRepository.Editor.Action> actions = new ArrayDeque<>();
        private final List<Descriptor> items;
        private volatile boolean used;

        Editor(List<Descriptor> items) {
            this.items = items;
        }

        @Override
        public Editor enqueue(AppsRepository.Editor.Action action) {
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
        public AppsRepository.Editor clear() {
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
                        Iterator<AppsRepository.Editor.Action> iter = actions.iterator();
                        while (iter.hasNext()) {
                            iter.next().apply(result);
                            iter.remove();
                        }
                        return result;
                    })
                    .doOnSubscribe(onSubscribe)
                    .doOnSuccess(LauncherAppsRepository.this::writeToDisk)
                    .flatMapCompletable(descriptors -> updater);
        }
    }
}
