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
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import com.italankin.lnch.R;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.*;

public class WidgetSizeHelper {

    private final AppWidgetManager appWidgetManager;
    private final DisplayMetrics displayMetrics;
    private final int widgetPaddings;

    public WidgetSizeHelper(Context context) {
        appWidgetManager = AppWidgetManager.getInstance(context);
        displayMetrics = context.getResources().getDisplayMetrics();
        widgetPaddings = context.getResources().getDimensionPixelSize(R.dimen.widget_padding) * 2;
    }

    /**
     * Resize widget with given options.
     *
     * @param appWidgetId     bound widget ID
     * @param options         additional options for widget, can be empty
     * @param width           current width of the widget
     * @param height          current height of the widget
     * @param paddingIncluded whether default padding is included in {@param width} and {@param height}
     */
    public void resize(int appWidgetId, @NonNull Bundle options, @Px int width, @Px int height, boolean paddingIncluded) {
        float density = displayMetrics.density;
        int widthDp = (int) (width / density);
        int heightDp = (int) (height / density);
        if (paddingIncluded) {
            widthDp -= (int) (widgetPaddings / density);
            heightDp -= (int) (widgetPaddings / density);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<Parcelable> sizes = new ArrayList<>(1);
            sizes.add(new SizeF(widthDp, heightDp));
            options.putParcelableArrayList(OPTION_APPWIDGET_SIZES, sizes);
        }
        options.putInt(OPTION_APPWIDGET_MIN_WIDTH, widthDp);
        options.putInt(OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
        options.putInt(OPTION_APPWIDGET_MAX_WIDTH, widthDp);
        options.putInt(OPTION_APPWIDGET_MAX_HEIGHT, heightDp);
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
    }

    /**
     * Calculates current size of the widget.
     *
     * @param info            widget provider info, obtained via {@link AppWidgetManager#getAppWidgetInfo(int)}
     * @param options         widget options, obtained via {@link AppWidgetManager#getAppWidgetOptions(int)}
     * @param includePaddings include paddings when calculating size
     * @return size (in px) of the widget
     */
    public Size getSize(AppWidgetProviderInfo info, Bundle options, boolean includePaddings) {
        if (info.resizeMode == AppWidgetProviderInfo.RESIZE_NONE) {
            return getMinSize(info, true);
        }
        float density = displayMetrics.density;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<SizeF> sizes = options.getParcelableArrayList(OPTION_APPWIDGET_SIZES);
            if (sizes != null) {
                SizeF min = null;
                for (SizeF size : sizes) {
                    if (min == null || min.getHeight() > size.getHeight()) {
                        min = size;
                    }
                }
                if (min != null) {
                    int width = (int) (min.getWidth() * density);
                    int height = (int) (min.getHeight() * density);
                    if (includePaddings) {
                        width += widgetPaddings;
                        height += widgetPaddings;
                    }
                    return new Size(width, height);
                }
            }
        }
        Size minSize = getMinSize(info, false);
        int widthDp = options.getInt(OPTION_APPWIDGET_MIN_WIDTH, (int) (minSize.getWidth() / density));
        int heightDp = options.getInt(OPTION_APPWIDGET_MIN_HEIGHT, (int) (minSize.getHeight() / density));
        int width = (int) (widthDp * density);
        int height = (int) (heightDp * density);
        if (includePaddings) {
            width += widgetPaddings;
            height += widgetPaddings;
        }
        return new Size(width, height);
    }

    /**
     * Calculates the absolute minimum size of the widget.
     *
     * @param info            widget provider info, obtained via {@link AppWidgetManager#getAppWidgetInfo(int)}
     * @param includePaddings include paddings when calculating size
     * @return minimum size (in px) of the widget
     */
    public Size getMinSize(AppWidgetProviderInfo info, boolean includePaddings) {
        int minWidth = info.minWidth;
        if (info.minResizeWidth > 0) {
            minWidth = Math.min(info.minWidth, info.minResizeWidth);
        }
        int minHeight = info.minHeight;
        if (info.minResizeHeight > 0) {
            minHeight = Math.min(info.minHeight, info.minResizeHeight);
        }
        if (includePaddings) {
            return new Size(minWidth + widgetPaddings, minHeight + widgetPaddings);
        } else {
            return new Size(minWidth, minHeight);
        }
    }
}
