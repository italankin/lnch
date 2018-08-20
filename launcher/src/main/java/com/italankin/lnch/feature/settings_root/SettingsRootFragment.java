package com.italankin.lnch.feature.settings_root;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.italankin.lnch.R;

public class SettingsRootFragment extends PreferenceFragmentCompat {
    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_root);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.prefs_home_customize).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.launchEditMode();
            }
            return true;
        });
        findPreference(R.string.prefs_search_behavior).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showSearchPreferences();
            }
            return true;
        });
        findPreference(R.string.prefs_wallpaper).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperPreferences();
            }
            return true;
        });
        findPreference(R.string.prefs_home_apps).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showAppsVisibilityPreferences();
            }
            return true;
        });
    }

    private Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }

    public interface Callbacks {
        void launchEditMode();

        void showSearchPreferences();

        void showAppsVisibilityPreferences();

        void showWallpaperPreferences();
    }
}
