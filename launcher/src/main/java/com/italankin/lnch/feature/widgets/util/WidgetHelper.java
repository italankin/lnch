package com.italankin.lnch.feature.widgets.util;

import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.os.Build;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.feature.widgets.WidgetsFragment;

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
}
