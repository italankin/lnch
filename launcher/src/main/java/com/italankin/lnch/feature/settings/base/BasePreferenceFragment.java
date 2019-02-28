package com.italankin.lnch.feature.settings.base;

import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(@StringRes int key) {
        return (T) findPreference(getString(key));
    }

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(Preferences.Pref<?> pref) {
        return (T) findPreference(pref.key());
    }
}
