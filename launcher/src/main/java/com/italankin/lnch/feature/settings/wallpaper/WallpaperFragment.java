package com.italankin.lnch.feature.settings.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class WallpaperFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    public static WallpaperFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        WallpaperFragment fragment = new WallpaperFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_wallpaper);
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
        findPreference(Preferences.WALLPAPER_OVERLAY_COLOR).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowWallpaperOverlay().result());
            return true;
        });
    }

    public static class ShowWallpaperOverlay extends SignalFragmentResultContract {
        public ShowWallpaperOverlay() {
            super("show_wallpaper_overlay");
        }
    }
}
