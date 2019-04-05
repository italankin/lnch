package com.italankin.lnch.model.repository.descriptor.apps.prefs;

import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.descriptor.sort.AscLabelSorter;
import com.italankin.lnch.model.repository.descriptor.sort.DescLabelSorter;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class SortTransform implements ApplyPreferences.Transform {

    @Override
    public AppsData apply(AppsData appsData, Preferences preferences) {
        switch (preferences.get(Preferences.APPS_SORT_MODE)) {
            case AZ: {
                boolean changed = new AscLabelSorter().sort(appsData.items);
                return appsData.copy(changed);
            }
            case ZA: {
                boolean changed = new DescLabelSorter().sort(appsData.items);
                return appsData.copy(changed);
            }
            case MANUAL:
            default:
                return appsData;
        }
    }
}
