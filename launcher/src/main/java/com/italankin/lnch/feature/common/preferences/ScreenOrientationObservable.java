package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.ScreenOrientation;

public class ScreenOrientationObservable extends PreferenceObservable<ScreenOrientation, SupportsOrientation> {

    public ScreenOrientationObservable(Preferences preferences) {
        super(preferences, Preferences.SCREEN_ORIENTATION);
    }

    @Override
    protected void onSubscribe(SupportsOrientation listener, ScreenOrientation currentValue) {
        listener.onOrientationChange(currentValue, false);
    }

    @Override
    protected void onValueChanged(SupportsOrientation listener, ScreenOrientation oldValue, ScreenOrientation newValue) {
        listener.onOrientationChange(newValue, oldValue != newValue);
    }
}
