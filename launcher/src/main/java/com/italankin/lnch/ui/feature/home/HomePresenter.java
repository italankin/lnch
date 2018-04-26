package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.model.AppItem;
import com.italankin.lnch.model.repository.apps.IAppsRepository;
import com.italankin.lnch.model.repository.search.ISearchRepository;
import com.italankin.lnch.ui.base.AppPresenter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

@InjectViewState
public class HomePresenter extends AppPresenter<IHomeView> {

    private final IAppsRepository appsRepository;
    private final ISearchRepository searchRepository;
    private final PackageManager packageManager;

    @Inject
    HomePresenter(IAppsRepository appsRepository, ISearchRepository searchRepository, PackageManager packageManager) {
        this.appsRepository = appsRepository;
        this.packageManager = packageManager;
        this.searchRepository = searchRepository;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadApps();
    }

    void loadApps() {
        getViewState().showProgress();
        Subscription s = appsRepository.updates()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppItem>>() {
                    @Override
                    protected void onNext(IHomeView viewState, List<AppItem> list) {
                        viewState.onAppsLoaded(list, searchRepository);
                    }

                    @Override
                    protected void onError(IHomeView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
        subs.add(s);
    }

    void swapItems(int from, int to) {
//        if (from < to) {
//            for (int i = from; i < to; i++) {
//                swapOrder(apps, i, i + 1);
//            }
//        } else {
//            for (int i = from; i > to; i--) {
//                swapOrder(apps, i, i - 1);
//            }
//        }
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    void saveState() {
//        updates.onNext(apps);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private static void swapOrder(List<AppItem> list, int i1, int i2) {
        AppItem left = list.get(i1);
        AppItem right = list.get(i2);
        int tmp = left.order;
        left.order = right.order;
        right.order = tmp;
        Collections.swap(list, i1, i2);
    }
}
