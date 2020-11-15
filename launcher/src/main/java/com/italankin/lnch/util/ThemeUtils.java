package com.italankin.lnch.util;

import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeUtils {

    public static void applyTheme(Preferences.ColorTheme colorTheme) {
        switch (colorTheme) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
            case SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private ThemeUtils() {
        // no instance
    }
}
