package com.italankin.lnch.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.feature.home.HomeActivity;
import com.italankin.lnch.feature.settings.SettingsActivity;

import java.net.URISyntaxException;

import timber.log.Timber;

public final class IntentUtils {

    public static boolean safeStartActivity(Context context, Intent intent) {
        return safeStartActivity(context, intent, null);
    }

    public static boolean safeStartActivity(Context context, Intent intent, Bundle options) {
        if (!canHandleIntent(context, intent)) {
            return false;
        }
        try {
            context.startActivity(intent, options);
            return true;
        } catch (ActivityNotFoundException e) {
            Timber.w(e, "safeStartActivity:");
            return false;
        }
    }

    public static boolean canHandleIntent(PackageManager packageManager, Intent intent) {
        if (intent == null) {
            return false;
        }
        ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);
        if (activityInfo != null) {
            return BuildConfig.APPLICATION_ID.equals(activityInfo.packageName) || activityInfo.exported;
        }
        return false;
    }

    /**
     * If we try to start {@link HomeActivity} from itself, start {@link SettingsActivity} instead.
     */
    public static Intent resolveSelfIntent(Context context, Intent intent) {
        ComponentName cn = intent.getComponent();
        if (cn != null && cn.getClassName().equals(HomeActivity.class.getCanonicalName())) {
            return SettingsActivity.getStartIntent(context);
        }
        return intent;
    }

    public static Intent fromUri(String uri) {
        return fromUri(uri, 0);
    }

    public static Intent fromUri(String uri, int flags) {
        try {
            return Intent.parseUri(uri, flags);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static boolean canHandleIntent(Context context, Intent intent) {
        return canHandleIntent(context.getPackageManager(), intent);
    }

    private IntentUtils() {
        // no instance
    }
}
