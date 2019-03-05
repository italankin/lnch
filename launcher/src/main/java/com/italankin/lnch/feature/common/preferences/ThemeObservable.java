package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

public class ThemeObservable extends PreferenceObservable<Preferences.ColorTheme, ThemedActivity> {

    public ThemeObservable(Preferences preferences) {
        super(preferences, Preferences.COLOR_THEME);
    }

    @Override
    protected void onSubscribe(ThemedActivity listener, Preferences.ColorTheme currentValue) {
        listener.onThemeChanged(currentValue, false);
    }

    @Override
    protected void onValueChanged(ThemedActivity listener, Preferences.ColorTheme oldValue, Preferences.ColorTheme newValue) {
        listener.onThemeChanged(newValue, oldValue != newValue);
    }
}
