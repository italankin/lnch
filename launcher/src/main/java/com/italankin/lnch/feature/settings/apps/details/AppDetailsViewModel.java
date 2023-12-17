package com.italankin.lnch.feature.settings.apps.details;

import androidx.annotation.Nullable;
import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.*;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import javax.inject.Inject;

public class AppDetailsViewModel extends AppViewModel {

    private final DescriptorRepository descriptorRepository;
    private final BehaviorSubject<AppDetailsModel> appDetailsModelSubject = BehaviorSubject.create();

    @Inject
    AppDetailsViewModel(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    Observable<AppDetailsModel> appDetailsEvents() {
        return appDetailsModelSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void loadDescriptor(String descriptorId) {
        Single.fromCallable(() -> {
                    return new AppDetailsModel(descriptorRepository.findById(AppDescriptor.class, descriptorId));
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(AppDetailsModel model) {
                        appDetailsModelSubject.onNext(model);
                    }

                    @Override
                    public void onError(Throwable e) {
                        appDetailsModelSubject.onError(e);
                    }
                });
    }

    void setCustomLabel(AppDetailsModel model, String newValue) {
        String customLabel = newValue == null || newValue.isEmpty() ? null : newValue;
        model.customLabel = customLabel;
        commitAction(
                new RenameAction(model.descriptor, customLabel),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customLabel=%s", model.descriptor, customLabel);
                    }
                }
        );
    }

    void setCustomColor(AppDetailsModel model, @Nullable Integer newColor) {
        model.customColor = newColor;
        commitAction(
                new SetColorAction(model.descriptor, newColor),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customColor=%s", model.descriptor, newColor);
                    }
                }
        );
    }

    void setCustomBadgeColor(AppDetailsModel model, @Nullable Integer newColor) {
        model.customBadgeColor = newColor;
        commitAction(
                new SetBadgeColorAction(model.descriptor, newColor),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: customColor=%s", model.descriptor, newColor);
                    }
                }
        );
    }

    void setIgnored(AppDetailsModel model, boolean ignored) {
        model.ignored = ignored;
        commitAction(
                new SetIgnoreAction(model.descriptor, ignored),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: ignored=%b", model.descriptor, ignored);
                    }
                }
        );
    }

    void setSearchVisible(AppDetailsModel model, boolean visible) {
        if (visible) {
            model.searchFlags |= AppDescriptor.FLAG_SEARCH_VISIBLE;
        } else {
            model.searchFlags &= ~AppDescriptor.FLAG_SEARCH_VISIBLE;
        }
        commitAction(
                new SetSearchFlagsAction(model.descriptor, model.searchFlags),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", model.descriptor, model.searchFlags);
                    }
                }
        );
    }

    void setShortcutsVisible(AppDetailsModel model, boolean showShortcuts) {
        model.showShortcuts = showShortcuts;
        commitAction(
                new ShortcutsVisibilityAction(model.descriptor, showShortcuts),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: showShortcuts=%b", model.descriptor, model.showShortcuts);
                    }
                });
    }

    void setSearchShortcutsVisible(AppDetailsModel model, boolean visible) {
        if (visible) {
            model.searchFlags |= AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        } else {
            model.searchFlags &= ~AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
        }
        commitAction(
                new SetSearchFlagsAction(model.descriptor, model.searchFlags),
                new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Update package=%s: searchFlags=%d", model.descriptor, model.searchFlags);
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
