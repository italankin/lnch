package com.italankin.lnch.feature.settings.base;

import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public abstract class AppPreferenceFragment extends PreferenceFragmentCompat {

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(@StringRes int key) {
        return (T) findPreference(getString(key));
    }
}
