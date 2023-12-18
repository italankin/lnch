package com.italankin.lnch.feature.home.repository.editmode;

import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.model.repository.prefs.Preferences;

class WallpaperDimProperty extends SimpleProperty<Integer> implements EditModeState.PreferenceProperty<Integer> {

    public WallpaperDimProperty() {
        super("wallpaper_dim");
    }

    @Override
    public void write(Preferences preferences, Integer newValue) {
        if (newValue != null) {
            preferences.set(Preferences.WALLPAPER_OVERLAY_SHOW, true);
            preferences.set(Preferences.WALLPAPER_OVERLAY_COLOR, newValue);
        } else {
            preferences.set(Preferences.WALLPAPER_OVERLAY_SHOW, false);
            preferences.reset(Preferences.WALLPAPER_OVERLAY_COLOR);
        }
    }
}
