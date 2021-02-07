package com.italankin.lnch.feature.settings.apps;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class AppsSettingsPresenter extends AppPresenter<AppsSettingsView> {

    private final DescriptorRepository descriptorRepository;

    @Inject
    AppsSettingsPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    protected void onFirstViewAttach() {
        loadApps();
    }

    void toggleAppVisibility(int position, AppDescriptorUi item) {
        boolean ignored = !item.isIgnored();
        item.setIgnored(ignored);
        getViewState().onItemChanged(position);

        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(item.getDescriptor(), !ignored))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }

    void loadApps() {
        getViewState().showLoading();
        Single
                .fromCallable(() -> {
                    List<AppDescriptor> appDescriptors = descriptorRepository.itemsOfType(AppDescriptor.class);
                    ArrayList<AppDescriptorUi> result = new ArrayList<>(appDescriptors.size());
                    for (AppDescriptor appDescriptor : appDescriptors) {
                        result.add(new AppDescriptorUi(appDescriptor));
                    }
                    Collections.sort(result, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER
                            .compare(lhs.getVisibleLabel(), rhs.getVisibleLabel()));
                    return result;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<AppDescriptorUi>>() {
                    @Override
                    protected void onSuccess(AppsSettingsView viewState, List<AppDescriptorUi> apps) {
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsSettingsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
