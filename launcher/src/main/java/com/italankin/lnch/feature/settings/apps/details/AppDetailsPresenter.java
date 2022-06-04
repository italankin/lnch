package com.italankin.lnch.feature.settings.apps.details;

import androidx.annotation.Nullable;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetBadgeColorAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetColorAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetSearchFlagsAction;
import com.italankin.lnch.model.repository.descriptor.actions.ShortcutsVisibilityAction;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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

    void setCustomLabel(AppDescriptor descriptor, String newValue) {
        String customLabel = newValue == null || newValue.isEmpty() ? null : newValue;
        descriptor.setCustomLabel(customLabel);
        commitAction(
                new RenameAction(descriptor, customLabel),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customLabel=%s", descriptor, customLabel);
                    }
                }
        );
    }

    void setCustomColor(AppDescriptor descriptor, @Nullable Integer newColor) {
        descriptor.setCustomColor(newColor);
        commitAction(
                new SetColorAction(descriptor, newColor),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customColor=%s", descriptor, newColor);
                    }
                }
        );
    }

    void setCustomBadgeColor(AppDescriptor descriptor, @Nullable Integer newColor) {
        descriptor.customBadgeColor = newColor;
        commitAction(
                new SetBadgeColorAction(descriptor, newColor),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customColor=%s", descriptor, newColor);
                    }
                }
        );
    }

    void setIgnored(AppDescriptor descriptor, boolean ignored) {
        descriptor.setIgnored(ignored);
        commitAction(
                new SetIgnoreAction(descriptor, ignored),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: ignored=%b", descriptor, ignored);
                    }
                }
        );
    }

    void setSearchVisible(AppDescriptor descriptor, boolean visible) {
        if (visible) {
            descriptor.searchFlags |= AppDescriptor.FLAG_SEARCH_VISIBLE;
        } else {
            descriptor.searchFlags &= ~AppDescriptor.FLAG_SEARCH_VISIBLE;
        }
        commitAction(
                new SetSearchFlagsAction(descriptor, descriptor.searchFlags),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", descriptor, descriptor.searchFlags);
                    }
                }
        );
    }

    void setShortcutsVisible(AppDescriptor descriptor, boolean showShortcuts) {
        descriptor.showShortcuts = showShortcuts;
        commitAction(
                new ShortcutsVisibilityAction(descriptor, showShortcuts),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: showShortcuts=%b", descriptor, descriptor.showShortcuts);
                    }
                });
    }

    void setSearchShortcutsVisible(AppDescriptor descriptor, boolean visible) {
        if (visible) {
            descriptor.searchFlags |= AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        } else {
            descriptor.searchFlags &= ~AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        }
        commitAction(
                new SetSearchFlagsAction(descriptor, descriptor.searchFlags),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", descriptor, descriptor.searchFlags);
                    }
                }
        );
    }

    private void commitAction(DescriptorRepository.Editor.Action action, CompletableState state) {
        descriptorRepository.edit()
                .enqueue(action)
                .commit()
                .subscribeOn(Schedulers.io())
                .subscribe(state);
    }
}
