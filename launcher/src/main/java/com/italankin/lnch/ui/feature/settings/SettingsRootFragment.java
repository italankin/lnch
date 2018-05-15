package com.italankin.lnch.ui.feature.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.italankin.lnch.R;

public class SettingsRootFragment extends AppPreferenceFragment {
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
        findPreference(R.string.prefs_edit_mode).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.launchEditMode();
            }
            return true;
        });
        findPreference(R.string.prefs_search).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showSearchPreferences();
            }
            return true;
        });
        findPreference(R.string.prefs_wallpaper).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpapersSelector();
            }
            return true;
        });
        findPreference(R.string.prefs_apps_visibility).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showAppsVisibilityPreferences();
            }
            return true;
        });
    }

    public interface Callbacks {
        void launchEditMode();

        void showSearchPreferences();

        void showAppsVisibilityPreferences();

        void showWallpapersSelector();
    }
}
