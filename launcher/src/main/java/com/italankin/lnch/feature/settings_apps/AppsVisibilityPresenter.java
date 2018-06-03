package com.italankin.lnch.feature.settings_apps;

import android.content.pm.PackageManager;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

@InjectViewState
public class AppsVisibilityPresenter extends AppPresenter<AppsVisibilityView> {

    private final AppsRepository appsRepository;
    private final AppsRepository.Editor editor;
    private final PackageManager packageManager;

    @Inject
    AppsVisibilityPresenter(PackageManager packageManager, AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
        this.editor = appsRepository.edit();
        this.packageManager = packageManager;
    }

    @Override
    protected void onFirstViewAttach() {
        loadApps();
    }

    void toggleAppVisibility(int position, AppViewModel item) {
        item.hidden = !item.hidden;
        editor.enqueue(new SetVisibilityAction(item.item, !item.hidden));
        getViewState().onItemChanged(position);
    }

    void saveChanges() {
        editor.commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }

    private void loadApps() {
        appsRepository.fetchApps()
                .toObservable()
                .map(appItems -> {
                    List<AppViewModel> apps = new ArrayList<>(appItems.size());
                    for (AppItem appItem : appItems) {
                        apps.add(new AppViewModel(appItem, packageManager));
                    }
                    return apps;
                })
                .subscribe(new State<List<AppViewModel>>() {
                    @Override
                    protected void onNext(AppsVisibilityView viewState, List<AppViewModel> apps) {
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsVisibilityView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }
}
