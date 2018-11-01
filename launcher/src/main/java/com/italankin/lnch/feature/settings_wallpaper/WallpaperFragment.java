package com.italankin.lnch.feature.settings_wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.italankin.lnch.R;

public class WallpaperFragment extends PreferenceFragmentCompat {

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
        addPreferencesFromResource(R.xml.prefs_wallpaper);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.key_wallpaper_change).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            Intent chooser = Intent.createChooser(intent,
                    getString(R.string.title_settings_wallpaper_change));
            startActivity(chooser);
            return true;
        });
        findPreference(R.string.pref_wallpaper_overlay_color).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperOverlayPreferences();
            }
            return true;
        });
    }

    private Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }

    public interface Callbacks {
        void showWallpaperOverlayPreferences();
    }
}
