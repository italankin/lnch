package com.italankin.lnch.feature.widgets.util;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SizeF;

import java.util.ArrayList;

public class WidgetSizeHelper {

    private final AppWidgetManager appWidgetManager;
    private final DisplayMetrics displayMetrics;

    public WidgetSizeHelper(Context context) {
        appWidgetManager = AppWidgetManager.getInstance(context);
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    public void resize(int appWidgetId, int width, int height) {
        resize(appWidgetId, new Bundle(), width, height);
    }

    public void resize(int appWidgetId, Bundle options, int width, int height) {
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, width);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, height);
        if ((info.resizeMode & AppWidgetProviderInfo.RESIZE_HORIZONTAL) == 0) {
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, width);
        }
        if ((info.resizeMode & AppWidgetProviderInfo.RESIZE_VERTICAL) == 0) {
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, height);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<Parcelable> sizes = new ArrayList<>(1);
            sizes.add(new SizeF(width / displayMetrics.density, height / displayMetrics.density));
            options.putParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES, sizes);
        }
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
    }

    public Size getMinSize(AppWidgetProviderInfo info, Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float density = displayMetrics.density;
            ArrayList<SizeF> sizes = options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
            SizeF min = null;
            if (sizes != null) {
                for (SizeF size : sizes) {
                    if (min == null || min.getWidth() > size.getWidth() || min.getHeight() > size.getHeight()) {
                        min = size;
                    }
                }
            }
            if (min != null) {
                return new Size((int) (min.getWidth() * density), (int) (min.getHeight() * density));
            }
        }
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, info.minWidth);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, info.minHeight);
        return new Size(minWidth, minHeight);
    }
}
