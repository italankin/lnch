package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

public interface SupportsOrientation {

    void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed);
}
