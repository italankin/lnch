package com.italankin.lnch.feature.settings.lookfeel;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.List;

public class LookAndFeelViewModel extends AppViewModel {

    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;
    private final boolean showOverlay;

    @Inject
    LookAndFeelViewModel(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
        this.showOverlay = preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW);
    }

    void saveData() {
        Boolean b = preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW);
        if (b == showOverlay) {
            return;
        }
        Integer color = b ? preferences.get(Preferences.APPS_COLOR_OVERLAY) : null;
        descriptorRepository.edit()
                .enqueue(new SetColor(color))
                .commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // do not unsubscribe to get changes saved
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "setAppsOverlayColor:");
                    }
                });
    }

    private static class SetColor implements DescriptorRepository.Editor.Action {
        private final Integer newColor;

        SetColor(Integer newColor) {
            this.newColor = newColor;
        }

        @Override
        public void apply(List<MutableDescriptor<?>> items) {
            for (MutableDescriptor<?> item : items) {
                if (item instanceof FolderDescriptor.Mutable) {
                    continue;
                }
                if (item instanceof CustomColorMutableDescriptor) {
                    ((CustomColorMutableDescriptor<?>) item).setCustomColor(newColor);
                }
            }
        }
    }
}
