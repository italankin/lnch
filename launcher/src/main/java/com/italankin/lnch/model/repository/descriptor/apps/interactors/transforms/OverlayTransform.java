package com.italankin.lnch.model.repository.descriptor.apps.interactors.transforms;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.PreferencesInteractor;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverlayTransform implements PreferencesInteractor.Transform {

    @Override
    public AppsData apply(AppsData appsData, Preferences preferences) {
        if (!preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW)) {
            return appsData;
        }
        Integer colorOverlay = preferences.get(Preferences.APPS_COLOR_OVERLAY);
        List<Descriptor> newDescriptors = new ArrayList<>(appsData.items.size());
        boolean changed = false;
        for (Descriptor item : appsData.items) {
            if (item instanceof CustomColorDescriptor) {
                Integer customColor = ((CustomColorDescriptor) item).getCustomColor();
                if (!Objects.equals(customColor, colorOverlay)) {
                    CustomColorMutableDescriptor<?> mutable = (CustomColorMutableDescriptor<?>) item.toMutable();
                    mutable.setCustomColor(colorOverlay);
                    newDescriptors.add(mutable.toDescriptor());
                    changed = true;
                    continue;
                }
            }
            newDescriptors.add(item);
        }
        return new AppsData(newDescriptors, appsData.changed || changed);
    }
}
