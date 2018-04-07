package com.italankin.lnch.model.provider;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public interface Provider<T> {
    T get(PackageManager pm, ResolveInfo ri);
}
