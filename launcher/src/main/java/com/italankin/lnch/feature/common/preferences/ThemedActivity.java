package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

public interface ThemedActivity {

    void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed);
}
