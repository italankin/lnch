package com.italankin.lnch.model.repository.shortcuts;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

public interface Shortcut {

    CharSequence getShortLabel();

    CharSequence getLongLabel();

    Uri getIconUri();

    boolean start(Rect bounds, Bundle options);
}
