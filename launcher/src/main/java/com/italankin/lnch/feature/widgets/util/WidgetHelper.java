package com.italankin.lnch.feature.widgets.util;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.feature.widgets.WidgetsFragment;
import timber.log.Timber;

public class WidgetHelper {

    public static boolean areWidgetsAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void resetAllWidgets() {
        if (areWidgetsAvailable()) {
            AppWidgetHost.deleteAllHosts();
            LauncherApp.daggerService.main().intentQueue().post(new Intent(WidgetsFragment.ACTION_RELOAD_WIDGETS));
        }
    }

    public static boolean isConfigureActivityExported(Context context, AppWidgetProviderInfo info) {
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(info.configure, 0);
            return activityInfo.exported;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "package not found:");
            return false;
        }
    }
}
