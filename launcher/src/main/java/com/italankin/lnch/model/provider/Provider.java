package com.italankin.lnch.model.provider;

import android.content.pm.LauncherActivityInfo;

public interface Provider<T> {
    T get(LauncherActivityInfo info);
}
