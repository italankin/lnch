package com.italankin.lnch.util;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.Nullable;

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

    public static Intent getPackageSystemSettings(String packageName) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, asUri(packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static ComponentName getComponentNameForPackage(PackageManager packageManager, String packageName) {
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        return intent != null ? intent.getComponent() : null;
    }

    public static Intent getUninstallIntent(String packageName) {
        return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, asUri(packageName));
    }

    public static ComponentName getGlobalSearchActivity(Context context) {
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchActivity = searchManager.getGlobalSearchActivity();
        if (searchActivity == null) {
            return null;
        }
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(searchActivity, 0);
            if (activityInfo == null || !activityInfo.exported) {
                return null;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
        return searchActivity;
    }

    @Nullable
    public static CharSequence getPackageLabel(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static Drawable getPackageIcon(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private PackageUtils() {
        // no instance
    }
}
