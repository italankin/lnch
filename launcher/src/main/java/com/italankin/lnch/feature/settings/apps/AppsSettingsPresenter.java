package com.italankin.lnch.feature.settings.apps;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@InjectViewState
public class AppsSettingsPresenter extends AppPresenter<AppsSettingsView> {

    private final DescriptorRepository descriptorRepository;

    @Inject
    AppsSettingsPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    protected void onFirstViewAttach() {
        observeApps();
    }

    void toggleAppVisibility(AppDescriptorUi item) {
        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(item.getDescriptor(), !item.isIgnored()))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }

    void observeApps() {
        getViewState().showLoading();
        descriptorRepository.observe(true)
                .map(descriptors -> {
                    ArrayList<AppDescriptorUi> list = new ArrayList<>();
                    for (Descriptor descriptor : descriptors) {
                        if (descriptor instanceof AppDescriptor) {
                            list.add(new AppDescriptorUi((AppDescriptor) descriptor));
                        }
                    }
                    Collections.sort(list, (lhs, rhs) -> {
                        return String.CASE_INSENSITIVE_ORDER.compare(lhs.getVisibleLabel(), rhs.getVisibleLabel());
                    });
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppDescriptorUi>>() {
                    @Override
                    protected void onNext(AppsSettingsView viewState, List<AppDescriptorUi> apps) {
                        viewState.onAppsUpdated(apps);
                    }

                    @Override
                    protected void onError(AppsSettingsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
