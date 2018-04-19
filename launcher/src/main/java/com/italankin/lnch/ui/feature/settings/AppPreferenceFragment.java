package com.italankin.lnch.ui.feature.settings;

import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

abstract class AppPreferenceFragment extends PreferenceFragmentCompat {
    protected Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }
}
