package com.italankin.lnch.model.repository.apps;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.provider.Preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

public class LauncherAppsRepository implements AppsRepository {
    private final Context context;
    private final PackageManager packageManager;
    private final Preferences preferences = new Preferences();
    private final LauncherApps launcherApps;
    private final Completable updater;
    private final BehaviorSubject<List<AppItem>> updatesSubject = BehaviorSubject.create();

    public LauncherAppsRepository(Context context, PackageManager packageManager) {
        this.context = context;
        this.packageManager = packageManager;
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.updater = loadAll()
                .doOnSuccess(appsData -> {
                    if (appsData.changed) {
                        Timber.d("data has changed, write to disk");
                        writeToDisk(appsData.apps);
                    }
                })
                .map(appsData -> appsData.apps)
                .doOnSuccess(updatesSubject::onNext)
                .ignoreElement();
    }

    @Override
    public Completable update() {
        return updater;
    }

    @Override
    public Observable<List<AppItem>> observeApps() {
        return updatesSubject;
    }

    @Override
    public Single<List<AppItem>> fetchApps() {
        return loadAll().map(appsData -> appsData.apps);
    }

    @Override
    public List<AppItem> getApps() {
        return updatesSubject.getValue();
    }

    @Override
    public AppsRepository.Editor edit() {
        return new Editor();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private Single<AppsData> loadAll() {
        return Single
                .fromCallable(() -> launcherApps.getActivityList(null, Process.myUserHandle()))
                .flatMap(infoList -> {
                    Single<AppsData> fromPm = loadFromList(infoList);
                    if (!getPrefs().exists()) {
                        return fromPm;
                    } else {
                        return loadFromFile(infoList)
                                .switchIfEmpty(fromPm)
                                .onErrorResumeNext(throwable -> {
                                    Timber.e(throwable, "loadAll:");
                                    return fromPm;
                                });
                    }
                });
    }

    private Single<AppsData> loadFromList(List<LauncherActivityInfo> infoList) {
        return Single.fromCallable(() -> {
            List<AppItem> apps = new ArrayList<>(16);
            for (int i = 0, s = infoList.size(); i < s; i++) {
                apps.add(createItem(infoList.get(i)));
            }
            Collections.sort(apps, AppItem.CMP_NAME_ASC);
            AppsData appsData = new AppsData();
            appsData.apps = apps;
            return appsData;
        });
    }

    private Maybe<AppsData> loadFromFile(List<LauncherActivityInfo> infoList) {
        return Maybe
                .create(emitter -> {
                    List<AppItem> savedItems = readFromDisk();
                    if (savedItems != null) {
                        List<AppItem> apps = new ArrayList<>(savedItems.size());
                        List<AppItem> deletedApps = new ArrayList<>(8);
                        Map<String, LauncherActivityInfo> infosByPackageName = new HashMap<>(infoList.size());
                        for (LauncherActivityInfo info : infoList) {
                            infosByPackageName.put(info.getApplicationInfo().packageName, info);
                        }
                        for (AppItem item : savedItems) {
                            LauncherActivityInfo info = infosByPackageName.remove(item.packageName);
                            if (info != null) {
                                int versionCode = getVersionCode(item.packageName);
                                if (item.versionCode != versionCode) {
                                    item.versionCode = versionCode;
                                    item.label = preferences.label.get(info);
                                    item.color = preferences.color.get(info);
                                }
                                apps.add(item);
                            } else {
                                deletedApps.add(item);
                            }
                        }
                        for (LauncherActivityInfo info : infosByPackageName.values()) {
                            apps.add(createItem(info));
                        }
                        AppsData appsData = new AppsData();
                        appsData.apps = apps;
                        appsData.changed = !deletedApps.isEmpty() || !infosByPackageName.isEmpty();
                        emitter.onSuccess(appsData);
                    }
                    emitter.onComplete();
                });
    }

    private AppItem createItem(LauncherActivityInfo info) {
        AppItem item = new AppItem(info.getApplicationInfo().packageName);
        item.versionCode = getVersionCode(info.getApplicationInfo().packageName);
        item.label = preferences.label.get(info);
        item.color = preferences.color.get(info);
        return item;
    }

    private int getVersionCode(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private void writeToDisk(List<AppItem> apps) {
        Timber.d("writeToDisk");
        try {
            FileWriter fw = new FileWriter(getPrefs());
            try {
                GsonBuilder builder = new GsonBuilder();
                if (BuildConfig.DEBUG) {
                    builder.setPrettyPrinting();
                }
                Gson gson = builder.create();
                String json = gson.toJson(apps);
                fw.write(json);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            Timber.e(e, "writeToDisk:");
        }
    }

    private List<AppItem> readFromDisk() {
        Timber.d("readFromDisk");
        Gson gson = new Gson();
        Type type = new TypeToken<List<AppItem>>() {
        }.getType();
        try {
            return gson.fromJson(new FileReader(getPrefs()), type);
        } catch (FileNotFoundException e) {
            Timber.e(e, "readFromDisk:");
            return null;
        }
    }

    private File getPrefs() {
        return new File(context.getFilesDir(), "packages.json");
    }

    private static Map<String, AppItem> mapByPackageName(List<AppItem> apps) {
        if (apps == null) {
            return new LinkedHashMap<>();
        }
        Map<String, AppItem> map = new LinkedHashMap<>(apps.size());
        for (AppItem app : apps) {
            map.put(app.packageName, app);
        }
        return map;
    }

    private static class AppsData {
        List<AppItem> apps;
        boolean changed;
    }

    final class Editor implements AppsRepository.Editor {
        private final Queue<AppsRepository.Editor.Action> actions = new ArrayDeque<>();
        private volatile boolean used;

        @Override
        public void enqueue(AppsRepository.Editor.Action action) {
            if (used) {
                throw new IllegalStateException();
            }
            actions.offer(action);
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
            return updatesSubject.take(1)
                    .doOnSubscribe(onSubscribe)
                    .doOnNext(apps -> {
                        Iterator<AppsRepository.Editor.Action> iter = actions.iterator();
                        while (iter.hasNext()) {
                            iter.next().apply(apps);
                            iter.remove();
                        }
                        writeToDisk(apps);
                    })
                    .ignoreElements();
        }
    }
}
