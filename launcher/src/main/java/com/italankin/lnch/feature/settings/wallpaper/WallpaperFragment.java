package com.italankin.lnch.feature.settings.wallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.NumberUtils;

public class WallpaperFragment extends BasePreferenceFragment {

    public static WallpaperFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        WallpaperFragment fragment = new WallpaperFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_REQUEST_KEY = "request_key";

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
            sendResult(new ShowWallpaperOverlay().result());
            return true;
        });
        int color = LauncherApp.daggerService
                .main()
                .preferences()
                .get(Preferences.WALLPAPER_OVERLAY_COLOR);
        pref.setSummary(String.format("#%08x", color));
    }

    private void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    public static class ShowWallpaperOverlay extends SignalFragmentResultContract {
        public ShowWallpaperOverlay() {
            super("show_wallpaper_overlay");
        }
    }
}
