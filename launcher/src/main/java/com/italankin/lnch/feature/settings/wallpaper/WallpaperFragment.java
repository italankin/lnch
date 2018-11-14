package com.italankin.lnch.feature.settings.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;

public class WallpaperFragment extends AppPreferenceFragment {

    private Callbacks callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        findPreference(R.string.key_wallpaper_change).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            Intent chooser = Intent.createChooser(intent,
                    getString(R.string.title_settings_wallpaper_change));
            startActivity(chooser);
            return true;
        });
        setupOverlayColor();
    }

    private void setupOverlayColor() {
        Preference pref = findPreference(R.string.pref_wallpaper_overlay_color);
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                int color = Integer.parseInt(String.valueOf(newValue));
                preference.setSummary(String.format("#%08x", color));
            } catch (NumberFormatException ignored) {
            }
            return true;
        });
        pref.setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperOverlayPreferences();
            }
            return true;
        });
        int color = LauncherApp.getInstance(requireContext())
                .daggerService
                .main()
                .getPreferences()
                .overlayColor();
        pref.setSummary(String.format("#%08x", color));
    }

    public interface Callbacks {
        void showWallpaperOverlayPreferences();
    }
}
