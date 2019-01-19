package com.italankin.lnch.feature.common.preferences;

import android.content.Context;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.ScreenOrientation;

public class ScreenOrientationObservable extends PreferenceObservable<ScreenOrientation, SupportsOrientation> {

    public ScreenOrientationObservable(Context context, Preferences preferences) {
        super(preferences, context.getString(R.string.pref_misc_screen_orientation), preferences.screenOrientation());
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
