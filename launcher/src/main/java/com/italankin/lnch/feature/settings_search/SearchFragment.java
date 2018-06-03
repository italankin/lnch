package com.italankin.lnch.feature.settings_search;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.italankin.lnch.R;

public class SearchFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_search);
    }
}
