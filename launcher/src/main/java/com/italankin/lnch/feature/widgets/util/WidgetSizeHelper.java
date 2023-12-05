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

import static android.appwidget.AppWidgetManager.*;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<Parcelable> sizes = new ArrayList<>(1);
            sizes.add(new SizeF(width / displayMetrics.density, height / displayMetrics.density));
            options.putParcelableArrayList(OPTION_APPWIDGET_SIZES, sizes);
        }
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
        options.putInt(OPTION_APPWIDGET_MIN_WIDTH, width);
        options.putInt(OPTION_APPWIDGET_MIN_HEIGHT, height);
        if ((info.resizeMode & AppWidgetProviderInfo.RESIZE_HORIZONTAL) == 0) {
            options.putInt(OPTION_APPWIDGET_MAX_WIDTH, width);
        }
        if ((info.resizeMode & AppWidgetProviderInfo.RESIZE_VERTICAL) == 0) {
            options.putInt(OPTION_APPWIDGET_MAX_HEIGHT, height);
        }
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
    }

    public Size getMinSize(AppWidgetProviderInfo info, Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float density = displayMetrics.density;
            ArrayList<SizeF> sizes = options.getParcelableArrayList(OPTION_APPWIDGET_SIZES);
            if (sizes != null) {
                SizeF min = null;
                for (SizeF size : sizes) {
                    if (min == null || min.getWidth() > size.getWidth() || min.getHeight() > size.getHeight()) {
                        min = size;
                    }
                }
                if (min != null) {
                    return new Size((int) (min.getWidth() * density), (int) (min.getHeight() * density));
                }
            }
        }
        Size minSize = getMinSizeFromInfo(info);
        int minWidth = options.getInt(OPTION_APPWIDGET_MIN_WIDTH, minSize.getWidth());
        int minHeight = options.getInt(OPTION_APPWIDGET_MIN_HEIGHT, minSize.getHeight());
        return new Size(minWidth, minHeight);
    }

    public Size getMinSizeFromInfo(AppWidgetProviderInfo info) {
        int minWidth = info.minWidth;
        if (info.minResizeWidth > 0) {
            minWidth = Math.min(info.minWidth, info.minResizeWidth);
        }
        int minHeight = info.minHeight;
        if (info.minResizeHeight > 0) {
            minHeight = Math.min(info.minHeight, info.minResizeHeight);
        }
        return new Size(minWidth, minHeight);
    }
}
