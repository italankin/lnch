package com.italankin.lnch.feature.settings_wallpaper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputFilter;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

public class WallpaperFragment extends PreferenceFragmentCompat {

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = ((LauncherApp) getContext().getApplicationContext())
                .daggerService
                .main()
                .getPreferences();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_wallpaper);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.prefs_wallpaper_change).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
            Intent chooser = Intent.createChooser(intent,
                    getString(R.string.title_settings_wallpaper_change));
            startActivity(chooser);
            return true;
        });
        findPreference(R.string.prefs_wallpaper_overlay_color).setOnPreferenceClickListener(preference -> {
            showOverlayColorDialog();
            return true;
        });
    }

    private void showOverlayColorDialog() {
        EditTextAlertDialog.builder(getContext())
                .setTitle(getString(R.string.title_settings_wallpaper_overlay_color))
                .customizeEditText(editText -> {
                    editText.setText(String.format("%08x", preferences.overlayColor()));
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String value = editText. getText().toString().trim();
                    if (value.length() == 8) {
                        try {
                            long decoded = Long.decode("0x" + value);
                            int color = (int) decoded;
                            preferences.setOverlayColor(color);
                        } catch (NumberFormatException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.customize_action_reset, (dialog, which) -> {
                    preferences.setOverlayColor(Color.TRANSPARENT);
                })
                .show();
    }

    private Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }
}
