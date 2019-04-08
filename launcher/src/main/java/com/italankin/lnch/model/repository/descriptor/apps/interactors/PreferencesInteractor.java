package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.List;

public class PreferencesInteractor {
    private final Preferences preferences;
    private final List<Transform> transforms;

    public PreferencesInteractor(Preferences preferences, List<Transform> transforms) {
        this.preferences = preferences;
        this.transforms = transforms;
    }

    public AppsData apply(AppsData source) {
        AppsData result = source;
        for (Transform transform : transforms) {
            result = transform.apply(result, preferences);
        }
        return result;
    }

    public interface Transform {
        AppsData apply(AppsData appsData, Preferences preferences);
    }
}
