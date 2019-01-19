package com.italankin.lnch.feature.common.preferences;

import android.content.Context;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class ThemeObservable extends PreferenceObservable<Preferences.ColorTheme, ThemedActivity> {

    public ThemeObservable(Context context, Preferences preferences) {
        super(preferences, context.getString(R.string.pref_misc_color_theme), preferences.colorTheme());
    }

    @Override
    void onSubscribe(ThemedActivity listener, Preferences.ColorTheme currentValue) {
        listener.onThemeChanged(currentValue, false);
    }

    @Override
    void onValueChanged(ThemedActivity listener, Preferences.ColorTheme oldValue, Preferences.ColorTheme newValue) {
        listener.onThemeChanged(newValue, oldValue != newValue);
    }

    @Override
    Preferences.ColorTheme getCurrentValue(Preferences preferences) {
        return preferences.colorTheme();
    }
}
