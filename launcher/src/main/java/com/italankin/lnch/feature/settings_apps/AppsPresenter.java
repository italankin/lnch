package com.italankin.lnch.feature.settings_apps;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.settings_apps.model.DecoratedAppViewModel;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;

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

    void toggleAppVisibility(int position, DecoratedAppViewModel item) {
        boolean hidden = !item.isHidden();
        item.setHidden(hidden);
        editor.enqueue(new SetVisibilityAction(item.getDescriptor(), hidden));
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
                .map(DecoratedAppViewModel::new)
                .sorted((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER
                        .compare(lhs.getVisibleLabel(), rhs.getVisibleLabel()))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<DecoratedAppViewModel>>() {
                    @Override
                    protected void onSuccess(AppsView viewState, List<DecoratedAppViewModel> apps) {
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
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        editor.clear();
                        loadApps();
                    }
                });
    }
}
