package com.italankin.lnch.feature.settings.apps.details;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetSearchFlagsAction;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

@InjectViewState
public class AppDetailsPresenter extends AppPresenter<AppDetailsView> {

    private final DescriptorRepository descriptorRepository;

    @Inject
    AppDetailsPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    void loadDescriptor(String descriptorId) {
        Single.fromCallable(() -> descriptorRepository.findById(AppDescriptor.class, descriptorId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<AppDescriptor>() {
                    @Override
                    protected void onSuccess(AppDetailsView viewState, AppDescriptor descriptor) {
                        viewState.onDescriptorLoaded(descriptor);
                    }

                    @Override
                    protected void onError(AppDetailsView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    void setIgnored(AppDescriptor descriptor, boolean ignored) {
        descriptor.setIgnored(ignored);
        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(descriptor, ignored))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: ignored=%b", descriptor, ignored);
                    }
                });
    }

    void setSearchVisible(AppDescriptor descriptor, boolean visible) {
        if (visible) {
            descriptor.searchFlags |= AppDescriptor.FLAG_SEARCH_VISIBLE;
        } else {
            descriptor.searchFlags &= ~AppDescriptor.FLAG_SEARCH_VISIBLE;
        }
        descriptorRepository.edit()
                .enqueue(new SetSearchFlagsAction(descriptor, descriptor.searchFlags))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", descriptor, descriptor.searchFlags);
                    }
                });
    }

    void setSearchShortcutsVisible(AppDescriptor descriptor, boolean visible) {
        if (visible) {
            descriptor.searchFlags |= AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        } else {
            descriptor.searchFlags &= ~AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        }
        descriptorRepository.edit()
                .enqueue(new SetSearchFlagsAction(descriptor, descriptor.searchFlags))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", descriptor, descriptor.searchFlags);
                    }
                });
    }
}
