package com.italankin.lnch.util.imageloader.resourceloader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import timber.log.Timber;

public class PackageResourceLoader implements ResourceLoader {

    private static final String SCHEME = "package.resource";

    public static Uri uriFrom(String packageName, @IdRes int id) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendPath(String.valueOf(id))
                .build();
    }

    private final PackageManager packageManager;

    public PackageResourceLoader(Context context) {
        packageManager = context.getPackageManager();
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @NonNull
    @Override
    public Drawable load(Uri uri) {
        try {
            String packageName = uri.getAuthority();
            String segment = uri.getLastPathSegment();
            if (segment == null) {
                return null;
            }
            int id = Integer.parseInt(segment);
            if (id == 0) {
                return null;
            }
            return packageManager
                    .getResourcesForApplication(packageName)
                    .getDrawableForDensity(id, Resources.getSystem().getDisplayMetrics().densityDpi);
        } catch (Exception e) {
            Timber.e(e, "load:");
            return null;
        }
    }
}
