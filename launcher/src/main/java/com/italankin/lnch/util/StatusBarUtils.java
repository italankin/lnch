package com.italankin.lnch.util;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

public final class StatusBarUtils {

    @SuppressLint("WrongConstant")
    public static void expandStatusBar(Context context) {
        try {
            Class<?> sbmClass = Class.forName("android.app.StatusBarManager");
            Method method = sbmClass.getMethod("expandNotificationsPanel");
            Object statusBarService = context.getSystemService("statusbar");
            method.invoke(statusBarService);
        } catch (Exception ignored) {
            // nothing to do
        }
    }

    private StatusBarUtils() {
        // no instance
    }
}
