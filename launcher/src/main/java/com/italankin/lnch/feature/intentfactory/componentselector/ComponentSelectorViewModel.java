package com.italankin.lnch.feature.intentfactory.componentselector;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.italankin.lnch.util.imageloader.resourceloader.ActivityIconLoader;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ComponentSelectorViewModel extends AppViewModel {

    private final PackageManager packageManager;
    private final BehaviorSubject<List<ComponentNameUi>> componentsSubject = BehaviorSubject.create();

    @Inject
    ComponentSelectorViewModel(Context context) {
        this.packageManager = context.getPackageManager();

        loadComponents();
    }

    Observable<List<ComponentNameUi>> componentsEvents() {
        return componentsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    private void loadComponents() {
        Single
                .fromCallable(() -> {
                    List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
                    List<ComponentNameUi> activities = new ArrayList<>(packages.size() * 2);
                    for (PackageInfo packageInfo : packages) {
                        if (packageInfo.activities == null) {
                            continue;
                        }
                        for (ActivityInfo activity : packageInfo.activities) {
                            if (!activity.exported || !activity.enabled) {
                                continue;
                            }
                            String visibleName = activity.name;
                            if (activity.name.contains(activity.packageName)) {
                                visibleName = activity.name.substring(activity.packageName.length());
                            }
                            Uri iconUri = ActivityIconLoader.uriFrom(activity.packageName, activity.name);
                            ComponentName componentName = new ComponentName(activity.packageName, activity.name);
                            ComponentNameUi ui = new ComponentNameUi(
                                    activity.packageName, visibleName, componentName, iconUri);
                            activities.add(ui);
                        }
                    }
                    return activities;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(List<ComponentNameUi> result) {
                        componentsSubject.onNext(result);
                    }
                });
    }
}
