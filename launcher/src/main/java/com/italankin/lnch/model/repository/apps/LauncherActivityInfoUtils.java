package com.italankin.lnch.model.repository.apps;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;

import java.util.Locale;

final class LauncherActivityInfoUtils {

    static String getLabel(LauncherActivityInfo info) {
        return info.getLabel()
                .toString()
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.getDefault());
    }

    static int getVersionCode(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    static String getComponentName(LauncherActivityInfo info) {
        return info.getComponentName().flattenToString();
    }

    static int getDominantIconColor(LauncherActivityInfo info, boolean darkTheme) {
        Bitmap bitmap = getIconBitmap(info.getIcon(0));
        int color = Palette.from(bitmap)
                .generate()
                .getDominantColor(darkTheme ? Color.WHITE : Color.BLACK);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (darkTheme) {
            if (hsv[2] < 0.25) {
                hsv[2] = 0.25f;
            }
        } else if (hsv[2] > 0.75) {
            hsv[2] = 0.75f;
        }
        return Color.HSVToColor(hsv);
    }

    private static Bitmap getIconBitmap(Drawable icon) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                Resources.getSystem().getDisplayMetrics());
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        icon.setBounds(0, 0, size, size);
        icon.draw(canvas);
        return bitmap;
    }

    private LauncherActivityInfoUtils() {
        // no instance
    }
}
