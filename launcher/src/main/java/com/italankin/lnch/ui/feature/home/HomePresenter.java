package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.PackageModel;
import com.italankin.lnch.model.provider.Preferences;
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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@InjectViewState
public class HomePresenter extends AppPresenter<IHomeView> {

    private final Context context;
    private final PackageManager packageManager;
    private final Preferences preferences = new Preferences();

    private List<PackageModel> apps;

    private final PublishSubject<List<PackageModel>> updates = PublishSubject.create();
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
                    Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                    return packageManager.queryIntentActivities(intent, 0);
                })
                .subscribeOn(Schedulers.computation())
                .flatMap(infoList -> {
                    Observable<List<PackageModel>> fromPm = loadFromPm(infoList);
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
                .subscribe(new State<List<PackageModel>>() {
                    @Override
                    protected void onNext(IHomeView viewState, List<PackageModel> list) {
                        apps = list;
                        viewState.onAppsLoaded(list);
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
        updates.onNext(apps);
    }

    void startSearch(Context context, String query) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + query));
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void startApp(Context context, PackageModel item) {
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void startAppSettings(Context context, PackageModel item) {
        Uri uri = Uri.fromParts("package", item.packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private File getPrefs() {
        return new File(context.getFilesDir(), "packages.json");
    }

    private Observable<List<PackageModel>> loadFromPm(List<ResolveInfo> infoList) {
        return Observable.fromCallable(() -> {
            List<PackageModel> apps = new ArrayList<>(16);
            for (int i = 0, s = infoList.size(); i < s; i++) {
                apps.add(createItem(packageManager, infoList.get(i)));
            }
            Collections.sort(apps, PackageModel.CMP_NAME_ASC);
            for (int i = 0, s = apps.size(); i < s; i++) {
                apps.get(i).order = i;
            }
            return apps;
        });
    }

    private Observable<List<PackageModel>> loadFromFile(List<ResolveInfo> infoList) {
        return Observable
                .create(emitter -> {
                    Map<String, PackageModel> map = readFromDisk();
                    if (map != null) {
                        List<PackageModel> apps = new ArrayList<>(map.size());
                        for (ResolveInfo ri : infoList) {
                            String packageName = ri.activityInfo.packageName;
                            if (map.containsKey(packageName)) {
                                PackageModel item = map.get(packageName);
                                item.packageName = packageName;
                                int versionCode = getVersionCode(packageManager, packageName);
                                if (item.versionCode != versionCode) {
                                    item.versionCode = versionCode;
                                    item.label = preferences.label.get(packageManager, ri);
                                    item.color = preferences.color.get(packageManager, ri);
                                }
                                apps.add(item);
                            } else {
                                PackageModel item = createItem(packageManager, ri);
                                item.order = -1;
                                apps.add(item);
                            }
                        }
                        // update order values
                        int offset = 0;
                        for (int i = 0, s = apps.size(); i < s; i++) {
                            PackageModel item = apps.get(i);
                            if (item.order < 0) {
                                item.order = i == 0 ? 0 : apps.get(i - 1).order + 1;
                                offset++;
                            } else {
                                item.order += offset;
                            }
                        }
                        Collections.sort(apps, PackageModel.CMP_ORDER);
                        emitter.onNext(apps);
                    }
                    emitter.onCompleted();
                }, Emitter.BackpressureMode.DROP);
    }

    @NonNull
    private PackageModel createItem(PackageManager pm, ResolveInfo ri) {
        PackageModel item = new PackageModel(ri.activityInfo.packageName);
        item.versionCode = getVersionCode(pm, ri.activityInfo.packageName);
        item.label = preferences.label.get(pm, ri);
        item.color = preferences.color.get(pm, ri);
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

    private void writeToDisk(List<PackageModel> apps) {
        Log.d("HomePresenter", "writeToDisk");
        Map<String, PackageModel> map = new LinkedHashMap<>(apps.size());
        for (PackageModel app : apps) {
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

    private Map<String, PackageModel> readFromDisk() {
        Log.d("HomePresenter", "readFromDisk");
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, PackageModel>>() {
        }.getType();
        try {
            return gson.fromJson(new FileReader(getPrefs()), type);
        } catch (FileNotFoundException e) {
            Log.e("HomePresenter", "readFromDisk:", e);
            return null;
        }
    }

    private static void swapOrder(List<PackageModel> list, int i1, int i2) {
        PackageModel left = list.get(i1);
        PackageModel right = list.get(i2);
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
                .debounce(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(this::writeToDisk, throwable -> {
                    Log.e("HomePresenter", "error receiving updates:", throwable);
                });
        subs.add(updatesSub);
    }
}
