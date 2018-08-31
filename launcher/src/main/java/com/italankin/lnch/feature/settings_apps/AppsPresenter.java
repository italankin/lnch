package com.italankin.lnch.feature.settings_apps;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class AppsPresenter extends AppPresenter<AppsView> {

    private final AppsRepository appsRepository;
    private final AppsRepository.Editor editor;

    @Inject
    AppsPresenter(AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
        this.editor = appsRepository.edit();
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
        appsRepository.observe()
                .subscribeOn(Schedulers.io())
                .take(1)
                .concatMapIterable(Functions.identity())
                .ofType(AppDescriptor.class)
                .map(AppViewModel::new)
                .sorted((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.label, rhs.label))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<AppViewModel>>() {
                    @Override
                    protected void onSuccess(AppsView viewState, List<AppViewModel> apps) {
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }

    void resetAppsSettings() {
        getViewState().showLoading();
        appsRepository.clear()
                .subscribeOn(Schedulers.io())
                .andThen(appsRepository.update())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        editor.clear();
                        loadApps();
                    }
                });
    }
}
