package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.ScreenOrientation;

public class ScreenOrientationObservable extends PreferenceObservable<ScreenOrientation, SupportsOrientation> {

    public ScreenOrientationObservable(Preferences preferences) {
        super(preferences, Preferences.Keys.SCREEN_ORIENTATION, preferences.screenOrientation());
    }

    @Override
    void onSubscribe(SupportsOrientation listener, ScreenOrientation currentValue) {
        listener.onOrientationChange(currentValue, false);
    }

    @Override
    void onValueChanged(SupportsOrientation listener, ScreenOrientation oldValue, ScreenOrientation newValue) {
        listener.onOrientationChange(newValue, oldValue != newValue);
    }

    @Override
    ScreenOrientation getCurrentValue(Preferences preferences) {
        return preferences.screenOrientation();
    }
}
