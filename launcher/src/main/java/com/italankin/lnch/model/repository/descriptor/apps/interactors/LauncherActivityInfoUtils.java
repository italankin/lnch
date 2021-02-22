package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import com.italankin.lnch.util.ResUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.palette.graphics.Palette;

final class LauncherActivityInfoUtils {

    private static final float[] TMP = new float[3];

    static long getVersionCode(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.getLongVersionCode();
            }
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
        Color.colorToHSV(color, TMP);
        if (darkTheme) {
            if (TMP[2] < 0.25) {
                TMP[2] = 0.25f;
            }
        } else if (TMP[2] > 0.75) {
            TMP[2] = 0.75f;
        }
        return Color.HSVToColor(TMP);
    }

    static Map<String, List<LauncherActivityInfo>> groupByPackage(List<LauncherActivityInfo> infoList) {
        Map<String, List<LauncherActivityInfo>> infosByPackageName = new LinkedHashMap<>(infoList.size());
        for (LauncherActivityInfo info : infoList) {
            String packageName = info.getApplicationInfo().packageName;
            List<LauncherActivityInfo> list = infosByPackageName.get(packageName);
            if (list == null) {
                list = new ArrayList<>(1);
                infosByPackageName.put(packageName, list);
            }
            list.add(info);
        }
        return infosByPackageName;
    }

    private static Bitmap getIconBitmap(Drawable icon) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                Resources.getSystem().getDisplayMetrics());
        return ResUtils.bitmapFromDrawable(icon, size, size);
    }

    private LauncherActivityInfoUtils() {
        // no instance
    }
}
