package com.italankin.lnch.feature.settings.apps.list;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetVisibilityAction;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class AppsListPresenter extends AppPresenter<AppsListView> {

    private final DescriptorRepository descriptorRepository;
    private final DescriptorRepository.Editor editor;

    @Inject
    AppsListPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
        this.editor = descriptorRepository.edit();
    }

    @Override
    protected void onFirstViewAttach() {
        loadApps();
    }

    void toggleAppVisibility(int position, AppViewModel item) {
        boolean hidden = !item.isHidden();
        item.setHidden(hidden);
        editor.enqueue(new SetVisibilityAction(item.getDescriptor(), !hidden));
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
        descriptorRepository.observe()
                .subscribeOn(Schedulers.io())
                .take(1)
                .concatMapIterable(Functions.identity())
                .ofType(AppDescriptor.class)
                .map(AppViewModel::new)
                .sorted((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER
                        .compare(lhs.getVisibleLabel(), rhs.getVisibleLabel()))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<AppViewModel>>() {
                    @Override
                    protected void onSuccess(AppsListView viewState, List<AppViewModel> apps) {
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsListView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
