package com.italankin.lnch.feature.settings.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.NumberUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

public class WallpaperFragment extends BasePreferenceFragment {

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_wallpaper_change).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            Intent chooser = Intent.createChooser(intent,
                    getString(R.string.settings_home_wallpaper_change));
            startActivity(chooser);
            return true;
        });
        setupOverlayColor();
    }

    private void setupOverlayColor() {
        Preference pref = findPreference(Preferences.WALLPAPER_OVERLAY_COLOR);
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            Integer color = NumberUtils.parseInt(String.valueOf(newValue));
            if (color != null) {
                preference.setSummary(String.format("#%08x", color));
            }
            return true;
        });
        pref.setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperOverlayPreferences();
            }
            return true;
        });
        int color = LauncherApp.daggerService
                .main()
                .preferences()
                .get(Preferences.WALLPAPER_OVERLAY_COLOR);
        pref.setSummary(String.format("#%08x", color));
    }

    public interface Callbacks {
        void showWallpaperOverlayPreferences();
    }
}
