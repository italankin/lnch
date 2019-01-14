package com.italankin.lnch.feature.settings.base;

import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(@StringRes int key) {
        return (T) findPreference(getString(key));
    }
}
