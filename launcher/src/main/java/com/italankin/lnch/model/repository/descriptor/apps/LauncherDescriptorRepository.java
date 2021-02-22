package com.italankin.lnch.model.repository.descriptor.apps;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Process;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.AppDescriptorInteractor;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.LoadFromFileInteractor;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.PreferencesInteractor;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.transforms.OverlayTransform;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.transforms.SortTransform;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class LauncherDescriptorRepository implements DescriptorRepository {

    private final DescriptorStore descriptorStore;
    private final PackagesStore packagesStore;
    private final LauncherApps launcherApps;
    private final Preferences preferences;

    private final AppDescriptorInteractor appDescriptorInteractor;
    private final LoadFromFileInteractor loadFromFileInteractor;
    private final PreferencesInteractor preferencesInteractor;

    private final Completable updater;
    private final BehaviorSubject<List<Descriptor>> updatesSubject = BehaviorSubject.create();

    public LauncherDescriptorRepository(Context context, PackageManager packageManager,
            DescriptorStore descriptorStore, PackagesStore packagesStore,
            ShortcutsRepository shortcutsRepository, Preferences preferences,
            NameNormalizer nameNormalizer) {
        this.descriptorStore = descriptorStore;
        this.packagesStore = packagesStore;
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.preferences = preferences;

        this.appDescriptorInteractor = new AppDescriptorInteractor(packageManager, preferences, nameNormalizer);
        this.loadFromFileInteractor = new LoadFromFileInteractor(appDescriptorInteractor,
                packagesStore, descriptorStore, shortcutsRepository, packageManager);
        this.preferencesInteractor = new PreferencesInteractor(preferences, Arrays.asList(
                new SortTransform(),
                new OverlayTransform()
        ));

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
    public <T extends Descriptor> T findById(Class<T> klass, String id) {
        for (T item : itemsOfType(klass)) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        throw new NoSuchElementException("No descriptor found for id=" + id);
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
        new LauncherAppsObservable(launcherApps)
                .debounce(300, TimeUnit.MILLISECONDS)
                .flatMapCompletable(event -> updater.onErrorComplete())
                .onErrorComplete(throwable -> {
                    Timber.e(throwable, "subscribeForUpdates (launcher apps)");
                    return true;
                })
                .subscribe();

        preferences.observe(Preferences.APPS_SORT_MODE)
                .filter(value -> value.get() != Preferences.AppsSortMode.MANUAL)
                .flatMapCompletable(s -> updater.onErrorComplete())
                .onErrorComplete(throwable -> {
                    Timber.e(throwable, "subscribeForUpdates (preferences)");
                    return true;
                })
                .subscribe();
    }

    private Completable createUpdater() {
        return loadAll()
                .map(preferencesInteractor::apply)
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
                    return loadFromFileInteractor.load(infoList)
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
                        items.add(appDescriptorInteractor.createItem(info));
                    }
                    return new AppsData(items, true);
                });
    }

    private void writeToDisk(List<Descriptor> items) {
        descriptorStore.write(packagesStore.output(), items);
    }

    final class Editor implements DescriptorRepository.Editor {
        private final Queue<DescriptorRepository.Editor.Action> actions = new ArrayDeque<>();
        private final List<Descriptor> items;
        private volatile boolean disposed;
        private final Consumer<Disposable> onSubscribe = d -> disposed = true;

        Editor(List<Descriptor> items) {
            this.items = items;
        }

        @Override
        public Editor enqueue(DescriptorRepository.Editor.Action action) {
            if (disposed) {
                throw new IllegalStateException("Editor is disposed");
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
            if (disposed) {
                throw new IllegalStateException("Editor is disposed");
            }
            if (actions.isEmpty()) {
                Timber.d("commit: no actions");
                return Completable.complete()
                        .doOnSubscribe(onSubscribe);
            }
            Timber.d("commit: apply actions");
            return Single
                    .fromCallable(() -> {
                        List<Descriptor> result = new ArrayList<>(items.size());
                        for (Descriptor item : items) {
                            result.add(item.copy());
                        }
                        Iterator<Action> iter = actions.iterator();
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
