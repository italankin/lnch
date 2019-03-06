package com.italankin.lnch.feature.settings.lookfeel;

import com.arellomobile.mvp.MvpView;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LookAndFeelPresenter extends AppPresenter<MvpView> {

    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    @Inject
    LookAndFeelPresenter(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    void save() {
        Integer color = null;
        if (preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW)) {
            color = preferences.get(Preferences.APPS_COLOR_OVERLAY);
        }
        descriptorRepository.edit()
                .enqueue(new SetColor(color))
                .commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
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
        public void apply(List<Descriptor> items) {
            for (Descriptor item : items) {
                if (item instanceof GroupDescriptor) {
                    continue;
                }
                if (item instanceof CustomColorDescriptor) {
                    ((CustomColorDescriptor) item).setCustomColor(newColor);
                }
            }
        }
    }
}
