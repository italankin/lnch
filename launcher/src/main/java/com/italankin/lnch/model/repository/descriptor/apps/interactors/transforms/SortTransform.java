package com.italankin.lnch.model.repository.descriptor.apps.interactors.transforms;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DividerDescriptor;
import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.PreferencesInteractor;
import com.italankin.lnch.model.repository.descriptor.sort.DescriptorSorter;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.Iterator;

public class SortTransform implements PreferencesInteractor.Transform {

    @Override
    public AppsData apply(AppsData appsData, Preferences preferences) {
        Preferences.AppsSortMode sortMode = preferences.get(Preferences.APPS_SORT_MODE);
        boolean removedDividers = false;
        if (sortMode != Preferences.AppsSortMode.MANUAL) {
            Iterator<Descriptor> iterator = appsData.items.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof DividerDescriptor) {
                    iterator.remove();
                    removedDividers = true;
                }
            }
        }
        switch (sortMode) {
            case AZ: {
                boolean changed = DescriptorSorter.LABEL_ASC.sort(appsData.items);
                return appsData.copy(changed || removedDividers);
            }
            case ZA: {
                boolean changed = DescriptorSorter.LABEL_DESC.sort(appsData.items);
                return appsData.copy(changed || removedDividers);
            }
            case MANUAL:
            default:
                return appsData;
        }
    }
}
