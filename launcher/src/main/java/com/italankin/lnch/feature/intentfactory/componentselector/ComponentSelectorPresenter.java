package com.italankin.lnch.feature.intentfactory.componentselector;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.italankin.lnch.util.imageloader.resourceloader.ActivityIconLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class ComponentSelectorPresenter extends AppPresenter<ComponentSelectorView> {

    private final PackageManager packageManager;

    @Inject
    ComponentSelectorPresenter(Context context) {
        this.packageManager = context.getPackageManager();
    }

    @Override
    protected void onFirstViewAttach() {
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<ComponentNameUi>>() {
                    @Override
                    protected void onSuccess(ComponentSelectorView viewState, List<ComponentNameUi> result) {
                        viewState.onComponentsLoaded(result);
                    }
                });
    }
}
