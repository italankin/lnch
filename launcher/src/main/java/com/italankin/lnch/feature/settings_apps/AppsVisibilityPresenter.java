package com.italankin.lnch.feature.settings_apps;

import android.content.pm.PackageManager;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;
import com.italankin.lnch.util.rx.ListMapper;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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

    void loadApps() {
        getViewState().showLoading();
        appsRepository.observeApps()
                .subscribeOn(Schedulers.io())
                .take(1)
                .map(ListMapper.create(item -> new AppViewModel(item, packageManager)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppViewModel>>() {
                    @Override
                    protected void onNext(AppsVisibilityView viewState, List<AppViewModel> apps) {
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsVisibilityView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
