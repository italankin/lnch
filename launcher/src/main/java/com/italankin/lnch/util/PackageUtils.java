package com.italankin.lnch.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public final class PackageUtils {

    public static boolean isSystem(PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Uri asUri(String packageName) {
        return Uri.fromParts("package", packageName, null);
    }

    private PackageUtils() {
        // no instance
    }
}
