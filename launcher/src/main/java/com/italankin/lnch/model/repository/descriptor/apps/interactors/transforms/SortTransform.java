package com.italankin.lnch.model.repository.descriptor.apps.interactors.transforms;

import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.descriptor.apps.interactors.PreferencesInteractor;
import com.italankin.lnch.model.repository.descriptor.sort.DescriptorSorter;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class SortTransform implements PreferencesInteractor.Transform {

    @Override
    public AppsData apply(AppsData appsData, Preferences preferences) {
        switch (preferences.get(Preferences.APPS_SORT_MODE)) {
            case AZ: {
                boolean changed = DescriptorSorter.LABEL_ASC.sort(appsData.items);
                return appsData.copy(changed);
            }
            case ZA: {
                boolean changed = DescriptorSorter.LABEL_DESC.sort(appsData.items);
                return appsData.copy(changed);
            }
            case MANUAL:
            default:
                return appsData;
        }
    }
}
