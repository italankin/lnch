package com.italankin.lnch.feature.settings.base;

import android.os.Bundle;
import android.support.annotation.XmlRes;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SimplePreferencesFragment extends PreferenceFragmentCompat {

    public static SimplePreferencesFragment newInstance(@XmlRes int preferenceResource) {
        SimplePreferencesFragment fragment = new SimplePreferencesFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PREFERENCES, preferenceResource);
        fragment.setArguments(arguments);
        return fragment;
    }

    private static final String ARG_PREFERENCES = "preferences";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            addPreferencesFromResource(arguments.getInt(ARG_PREFERENCES));
        }
    }
}
