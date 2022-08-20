package com.italankin.lnch.util.imageloader.resourceloader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;

public class PackageIconLoader implements ResourceLoader {

    private static final String SCHEME = "package";

    public static Uri uriFrom(String packageName) {
        return new Uri.Builder().scheme(SCHEME).authority(packageName).build();
    }

    private final PackageManager packageManager;

    public PackageIconLoader(Context context) {
        this.packageManager = context.getPackageManager();
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @NonNull
    @Override
    public Drawable load(Uri uri) {
        try {
            return packageManager.getApplicationIcon(uri.getAuthority());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
