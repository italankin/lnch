package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.AppItem;
import com.italankin.lnch.model.provider.Preferences;
import com.italankin.lnch.model.searchable.GoogleSearchable;
import com.italankin.lnch.model.searchable.ISearchable;
import com.italankin.lnch.ui.base.AppPresenter;

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

import javax.inject.Inject;

import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@InjectViewState
public class HomePresenter extends AppPresenter<IHomeView> {

    private static final List<ISearchable> SEARCH_FALLBACKS =
            Collections.singletonList(new GoogleSearchable());

    private final Context context;
    private final PackageManager packageManager;
    private final Preferences preferences = new Preferences();

    private List<AppItem> apps;
    private volatile int appsHash;

    private final PublishSubject<List<AppItem>> updates = PublishSubject.create();
    private Subscription updatesSub;

    @Inject
    HomePresenter(Context context, PackageManager packageManager) {
        this.context = context;
        this.packageManager = packageManager;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadApps();
    }

    void loadApps() {
        getViewState().showProgress();
        Subscription s = Observable
                .fromCallable(() -> {
                    LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                    //noinspection ConstantConditions
                    return launcherApps.getActivityList(null, Process.myUserHandle());
                })
                .subscribeOn(Schedulers.computation())
                .flatMap(infoList -> {
                    Observable<List<AppItem>> fromPm = loadFromList(infoList);
                    if (!getPrefs().exists()) {
                        return fromPm;
                    } else {
                        return loadFromFile(infoList)
                                .switchIfEmpty(fromPm)
                                .onErrorResumeNext(fromPm);
                    }
                })
                .doOnNext(this::writeToDisk)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppItem>>() {
                    @Override
                    protected void onNext(IHomeView viewState, List<AppItem> list) {
                        apps = list;
                        appsHash = apps.hashCode();
                        viewState.onAppsLoaded(list, SEARCH_FALLBACKS);
                        subscribeForUpdates();
                    }

                    @Override
                    protected void onError(IHomeView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
        subs.add(s);
    }

    void swapItems(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                swapOrder(apps, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                swapOrder(apps, i, i - 1);
            }
        }
    }

    void startSearch(Context context, String query) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + query));
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void startApp(Context context, AppItem item) {
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void startAppSettings(Context context, AppItem item) {
        Uri uri = Uri.fromParts("package", item.packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void saveState() {
        updates.onNext(apps);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private File getPrefs() {
        return new File(context.getFilesDir(), "packages.json");
    }

    private Observable<List<AppItem>> loadFromList(List<LauncherActivityInfo> infoList) {
        return Observable.fromCallable(() -> {
            List<AppItem> apps = new ArrayList<>(16);
            for (int i = 0, s = infoList.size(); i < s; i++) {
                apps.add(createItem(packageManager, infoList.get(i)));
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
                                int versionCode = getVersionCode(packageManager, packageName);
                                if (item.versionCode != versionCode) {
                                    item.versionCode = versionCode;
                                    item.label = preferences.label.get(info);
                                    item.color = preferences.color.get(info);
                                }
                                apps.add(item);
                            } else {
                                newApps.add(createItem(packageManager, info));
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

    @NonNull
    private AppItem createItem(PackageManager pm, LauncherActivityInfo info) {
        AppItem item = new AppItem(info.getApplicationInfo().packageName);
        item.versionCode = getVersionCode(pm, info.getApplicationInfo().packageName);
        item.label = preferences.label.get(info);
        item.color = preferences.color.get(info);
        return item;
    }

    private static int getVersionCode(PackageManager pm, String packageName) {
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private void writeToDisk(List<AppItem> apps) {
        Log.d("HomePresenter", "writeToDisk");
        Map<String, AppItem> map = new LinkedHashMap<>(apps.size());
        for (AppItem app : apps) {
            map.put(app.packageName, app);
        }
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
            Log.e("HomePresenter", "writeToDisk:", e);
        }
    }

    private Map<String, AppItem> readFromDisk() {
        Log.d("HomePresenter", "readFromDisk");
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, AppItem>>() {
        }.getType();
        try {
            return gson.fromJson(new FileReader(getPrefs()), type);
        } catch (FileNotFoundException e) {
            Log.e("HomePresenter", "readFromDisk:", e);
            return null;
        }
    }

    private static void swapOrder(List<AppItem> list, int i1, int i2) {
        AppItem left = list.get(i1);
        AppItem right = list.get(i2);
        int tmp = left.order;
        left.order = right.order;
        right.order = tmp;
        Collections.swap(list, i1, i2);
    }

    private void subscribeForUpdates() {
        if (updatesSub != null && !updatesSub.isUnsubscribed()) {
            return;
        }
        updatesSub = updates
                .subscribeOn(Schedulers.io())
                .filter(items -> items.hashCode() != appsHash)
                .doOnNext(this::writeToDisk)
                .subscribe(items -> appsHash = items.hashCode(), throwable -> {
                    Log.e("HomePresenter", "error receiving updates:", throwable);
                });
        subs.add(updatesSub);
    }
}
