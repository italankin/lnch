package com.italankin.lnch.util.imageloader.resourceloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;

public class ActivityIconLoader implements ResourceLoader {

    private static final String SCHEME = "activity";

    public static Uri uriFrom(String packageName, String className) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendEncodedPath(className)
                .build();
    }

    private final PackageManager packageManager;

    public ActivityIconLoader(Context context) {
        packageManager = context.getPackageManager();
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @NonNull
    @Override
    public Drawable load(Uri uri) {
        String packageName = uri.getAuthority();
        String className = uri.getLastPathSegment();
        try {
            return packageManager.getActivityIcon(new ComponentName(packageName, className));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
