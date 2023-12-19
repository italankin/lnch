package com.italankin.lnch.util;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import androidx.annotation.Nullable;
import com.italankin.lnch.BuildConfig;
import timber.log.Timber;

import java.net.URISyntaxException;

public final class IntentUtils {

    public static final DefaultErrorConsumer DEFAULT_ERROR_CONSUMER = new DefaultErrorConsumer();

    public static boolean safeStartActivity(Context context, Intent intent) {
        return safeStartActivity(context, intent, null);
    }

    public static boolean safeStartActivity(Context context, Intent intent, @Nullable ErrorConsumer errorConsumer) {
        if (!canHandleIntent(context, intent)) {
            return false;
        }
        try {
            context.startActivity(intent, null);
            return true;
        } catch (Exception e) {
            if (errorConsumer != null) {
                errorConsumer.onError(context, e);
            }
            Timber.w(e, "safeStartActivity:");
            return false;
        }
    }

    public static boolean safeStartMainActivity(Context context, @Nullable ComponentName componentName,
            @Nullable View boundsView) {
        Rect bounds = ViewUtils.getViewBounds(boundsView);
        Bundle opts = IntentUtils.getActivityLaunchOptions(boundsView, bounds);
        return IntentUtils.safeStartMainActivity(context, componentName, bounds, opts);
    }

    public static boolean safeStartMainActivity(Context context, @Nullable ComponentName componentName,
            @Nullable Rect bounds, @Nullable Bundle opts) {
        if (componentName == null) {
            return false;
        }
        try {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            launcherApps.startMainActivity(componentName, Process.myUserHandle(), bounds, opts);
            return true;
        } catch (Exception e) {
            Timber.w(e, "safeStartMainActivity:");
            return false;
        }
    }

    public static boolean safeStartAppSettings(Context context, String packageName, @Nullable View view) {
        ComponentName componentName = PackageUtils.getComponentNameForPackage(context.getPackageManager(), packageName);
        if (componentName != null) {
            Rect bounds = ViewUtils.getViewBounds(view);
            Bundle options = IntentUtils.getActivityLaunchOptions(view, bounds);
            return IntentUtils.safeStartAppSettings(context, componentName, bounds, options);
        } else {
            Intent intent = PackageUtils.getPackageSystemSettings(packageName);
            return IntentUtils.safeStartActivity(context, intent);
        }
    }

    public static boolean safeStartAppSettings(Context context, @Nullable ComponentName componentName,
            @Nullable Rect bounds, @Nullable Bundle opts) {
        if (componentName == null) {
            return false;
        }
        try {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            launcherApps.startAppDetailsActivity(componentName, Process.myUserHandle(), bounds, opts);
            return true;
        } catch (Exception e) {
            Timber.w(e, "safeStartAppSettings");
            return false;
        }
    }

    public static boolean canHandleIntent(PackageManager packageManager, @Nullable Intent intent) {
        if (intent == null) {
            return false;
        }
        ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);
        if (activityInfo != null) {
            return BuildConfig.APPLICATION_ID.equals(activityInfo.packageName) || activityInfo.exported;
        }
        return false;
    }

    public static Intent fromUri(String uri) {
        return fromUri(uri, 0);
    }

    @Nullable
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

    @Nullable
    public static Bundle getActivityLaunchOptions(View view, Rect bounds) {
        if (view == null || bounds == null) {
            return null;
        }
        ActivityOptions activityOptions = ActivityOptions.makeClipRevealAnimation(view,
                0, 0,
                bounds.width(), bounds.height());
        return activityOptions.toBundle();
    }

    public interface ErrorConsumer {
        void onError(Context context, Exception e);
    }

    public static class DefaultErrorConsumer implements ErrorConsumer {

        @Override
        public void onError(Context context, Exception e) {
            ErrorUtils.showErrorDialogOrToast(context, e);
        }
    }

    private IntentUtils() {
        // no instance
    }
}
