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
import com.italankin.lnch.model.AppItem;
import com.italankin.lnch.model.provider.Preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Emitter;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class LauncherAppsRepository implements IAppsRepository {
    private final Context context;
    private final PackageManager packageManager;
    private final Preferences preferences = new Preferences();
    private final LauncherApps launcherApps;

    private BehaviorSubject<List<AppItem>> updates = BehaviorSubject.create();

    public LauncherAppsRepository(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    @Override
    public void reload() {
        loadAll()
                .subscribeOn(Schedulers.computation())
                .doOnNext(this::writeToDisk)
                .concatMapIterable(appItems -> appItems)
                .filter(appItem -> !appItem.hidden)
                .toList()
                .subscribe(new Observer<List<AppItem>>() {
                    @Override
                    public void onNext(List<AppItem> appItems) {
                        updates.onNext(appItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    @Override
    public Observable<List<AppItem>> updates() {
        try {
            return updates;
        } finally {
            if (!updates.hasValue()) {
                reload();
            }
        }
    }

    @Override
    public List<AppItem> getApps() {
        return updates.getValue();
    }

    @Override
    public void swapAppsOrder(int from, int to) {
        List<AppItem> list = getApps();
        if (list == null) {
            return;
        }
        if (from < to) {
            for (int i = from; i < to; i++) {
                swapOrder(list, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                swapOrder(list, i, i - 1);
            }
        }
    }

    @Override
    public void writeChanges() {
        loadAll()
                .subscribeOn(Schedulers.computation())
                .map(this::mapByPackageName)
                .doOnNext(apps -> {
                    List<AppItem> items = updates.getValue();
                    if (items != null) {
                        for (AppItem item : items) {
                            if (apps.containsKey(item.packageName)) {
                                apps.put(item.packageName, item);
                            }
                        }
                    }
                })
                .subscribe(new Observer<Map<String, AppItem>>() {
                    @Override
                    public void onNext(Map<String, AppItem> apps) {
                        writeToDisk(apps);
                        reload();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private Observable<List<AppItem>> loadAll() {
        return Observable
                .fromCallable(() -> launcherApps.getActivityList(null, Process.myUserHandle()))
                .flatMap(infoList -> {
                    Observable<List<AppItem>> fromPm = loadFromList(infoList);
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

    private Observable<List<AppItem>> loadFromList(List<LauncherActivityInfo> infoList) {
        return Observable.fromCallable(() -> {
            List<AppItem> apps = new ArrayList<>(16);
            for (int i = 0, s = infoList.size(); i < s; i++) {
                apps.add(createItem(infoList.get(i)));
            }
            Collections.sort(apps, AppItem.CMP_NAME_ASC);
            for (int i = 0, s = apps.size(); i < s; i++) {
                apps.get(i).order = i;
            }
            return apps;
        });
    }

    private Observable<List<AppItem>> loadFromFile(List<LauncherActivityInfo> infoList) {
        return Observable
                .create(emitter -> {
                    Map<String, AppItem> map = readFromDisk();
                    if (map != null) {
                        List<AppItem> apps = new ArrayList<>(map.size());
                        List<AppItem> newApps = new ArrayList<>(8);
                        int order = 0;
                        for (LauncherActivityInfo info : infoList) {
                            String packageName = info.getApplicationInfo().packageName;
                            if (map.containsKey(packageName)) {
                                AppItem item = map.get(packageName);
                                if (item.order > order) {
                                    order = item.order;
                                }
                                item.packageName = packageName;
                                int versionCode = getVersionCode(packageName);
                                if (item.versionCode != versionCode) {
                                    item.versionCode = versionCode;
                                    item.label = preferences.label.get(info);
                                    item.color = preferences.color.get(info);
                                }
                                apps.add(item);
                            } else {
                                newApps.add(createItem(info));
                            }
                        }
                        // update order values
                        if (newApps.size() > 0) {
                            for (int i = 0, s = newApps.size(); i < s; i++) {
                                AppItem item = newApps.get(i);
                                item.order = ++order;
                                apps.add(item);
                            }
                        }
                        Collections.sort(apps, AppItem.CMP_ORDER);
                        emitter.onNext(apps);
                    }
                    emitter.onCompleted();
                }, Emitter.BackpressureMode.DROP);
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

    private void writeToDisk(Map<String, AppItem> map) {
        Timber.d("writeToDisk");
        try {
            FileWriter fw = new FileWriter(getPrefs());
            try {
                GsonBuilder builder = new GsonBuilder();
                if (BuildConfig.DEBUG) {
                    builder.setPrettyPrinting();
                }
                Gson gson = builder.create();
                String json = gson.toJson(map);
                fw.write(json);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            Timber.e(e, "writeToDisk:");
        }
    }

    private void writeToDisk(List<AppItem> apps) {
        writeToDisk(mapByPackageName(apps));
    }

    private Map<String, AppItem> mapByPackageName(List<AppItem> apps) {
        Map<String, AppItem> map = new LinkedHashMap<>(apps.size());
        for (AppItem app : apps) {
            map.put(app.packageName, app);
        }
        return map;
    }

    private Map<String, AppItem> readFromDisk() {
        Timber.d("readFromDisk");
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, AppItem>>() {
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

    private static void swapOrder(List<AppItem> list, int from, int to) {
        AppItem left = list.get(from);
        AppItem right = list.get(to);
        int tmp = left.order;
        left.order = right.order;
        right.order = tmp;
        Collections.swap(list, from, to);
    }
}
