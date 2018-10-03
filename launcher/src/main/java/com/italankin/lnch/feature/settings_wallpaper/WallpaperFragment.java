package com.italankin.lnch.feature.settings_wallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings_wallpaper.overlay.WallpaperOverlayActivity;

public class WallpaperFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_wallpaper);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.prefs_wallpaper_change).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            Intent chooser = Intent.createChooser(intent,
                    getString(R.string.title_settings_wallpaper_change));
            startActivity(chooser);
            return true;
        });
        findPreference(R.string.prefs_wallpaper_overlay_color).setOnPreferenceClickListener(preference -> {
            Intent intent = WallpaperOverlayActivity.getStartIntent(getContext());
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            return true;
        });
    }

    private Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }
}
