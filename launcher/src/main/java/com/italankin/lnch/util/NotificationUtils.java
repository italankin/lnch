package com.italankin.lnch.util;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.italankin.lnch.NotificationListener;

import androidx.annotation.RequiresApi;

public final class NotificationUtils {

    public static boolean isNotificationAccessGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return isNotificationAccessGrantedO(context);
        } else {
            return isNotificationAccessGrantedPreO(context);
        }
    }

    private static boolean isNotificationAccessGrantedPreO(Context context) {
        String theList = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (theList == null) {
            return false;
        }
        String me = new ComponentName(context, NotificationListener.class).flattenToString();
        String[] listeners = theList.split(":");
        for (String next : listeners) {
            if (me.equals(next)) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private static boolean isNotificationAccessGrantedO(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        ComponentName listener = new ComponentName(context, NotificationListener.class);
        return nm.isNotificationListenerAccessGranted(listener);
    }

    private NotificationUtils() {
        // no instance
    }
}
