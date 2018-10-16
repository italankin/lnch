package com.italankin.lnch.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import com.italankin.lnch.BuildConfig;

import timber.log.Timber;

public final class IntentUtils {

    public static Intent getPackageSystemSettings(String packageName) {
        Uri uri = PackageUtils.asUri(packageName);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getUninstallIntent(String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri);
    }

    public static boolean safeStartActivity(Context context, Intent intent) {
        if (!canHandleIntent(context, intent)) {
            return false;
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Timber.w(e, "safeStartActivity:");
            return false;
        }
    }

    public static boolean canHandleIntent(PackageManager packageManager, Intent intent) {
        ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);
        if (activityInfo != null) {
            return BuildConfig.APPLICATION_ID.equals(activityInfo.packageName) || activityInfo.exported;
        }
        return false;
    }

    public static boolean canHandleIntent(Context context, Intent intent) {
        return canHandleIntent(context.getPackageManager(), intent);
    }

    private IntentUtils() {
        // no instance
    }
}
