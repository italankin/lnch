package com.italankin.lnch.feature.widgets.util;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SizeF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.CellSize;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.*;

public class WidgetSizeHelper {

    private static final float MAX_HEIGHT_FACTOR = .75f;

    private final AppWidgetManager appWidgetManager;
    private final DisplayMetrics displayMetrics;
    private final int widgetPaddings;

    public WidgetSizeHelper(Context context) {
        appWidgetManager = AppWidgetManager.getInstance(context);
        displayMetrics = context.getResources().getDisplayMetrics();
        widgetPaddings = context.getResources().getDimensionPixelSize(R.dimen.widget_padding) * 2;
    }

    public static Size calculateSizeForCell(Context context, int gridSize, float heightCellRatio) {
        Resources res = context.getResources();
        int margins = res.getDimensionPixelSize(R.dimen.widget_list_margin) * 2;
        DisplayMetrics dm = res.getDisplayMetrics();
        int size = Math.min(dm.widthPixels - margins, dm.heightPixels) / gridSize;
        int maxCellSize = res.getDimensionPixelSize(R.dimen.widget_max_cell_size);
        int cellWidth = Math.min(size, maxCellSize);
        int cellHeight = (int) (cellWidth * heightCellRatio);
        return new Size(cellWidth, cellHeight);
    }


    public static int calculateMaxHeightCells(Context context, int cellHeight) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int maxSize = (int) (dm.heightPixels * MAX_HEIGHT_FACTOR);
        return maxSize / cellHeight;
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
        if (appWidgetId == INVALID_APPWIDGET_ID) {
            return;
        }
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
     * Calculates (or retrieves saved) {@link AppWidget.Size} of the widget.
     *
     * @param cellSize   current cell size
     * @param info       provider info of the widget
     * @param options    current widget options
     * @param widgetData saved widget data, if any
     * @return size of the widget
     */
    public AppWidget.Size getAppWidgetSize(CellSize cellSize,
            AppWidgetProviderInfo info,
            Bundle options,
            @Nullable Preferences.Widget widgetData) {
        int maxAvailWidth = cellSize.maxAvailableWidth();
        int maxAvailHeight = cellSize.maxAvailableHeight();
        Size minSize = getMinSize(info, true);
        int minWidth = minMultipleOfCellSize(cellSize.width, minSize.getWidth(), maxAvailWidth);
        int minHeight = minMultipleOfCellSize(cellSize.height, minSize.getHeight(), maxAvailHeight);
        int targetCellWidth = 0, targetCellHeight = 0;
        if (widgetData != null) {
            targetCellWidth = widgetData.widthCells;
            targetCellHeight = widgetData.heightCells;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            targetCellWidth = info.targetCellWidth;
            targetCellHeight = info.targetCellHeight;
        }
        int width, height;
        Size size = getSize(info, options, true);
        if (targetCellWidth > 0) {
            width = Math.max(cellSize.width * Math.min(cellSize.widthCells, targetCellWidth), minWidth);
        } else {
            width = minMultipleOfCellSize(cellSize.width, size.getWidth(), maxAvailWidth);
        }
        if (targetCellHeight > 0) {
            height = Math.max(cellSize.height * Math.min(cellSize.heightCells, targetCellHeight), minHeight);
        } else {
            height = minMultipleOfCellSize(cellSize.height, size.getHeight(), maxAvailHeight);
        }
        int maxWidth = maxAvailWidth, maxHeight = maxAvailHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (info.maxResizeWidth > minWidth) {
                maxWidth = minMultipleOfCellSize(cellSize.width, info.maxResizeWidth, maxAvailWidth);
            }
            if (info.maxResizeHeight > minHeight) {
                maxHeight = minMultipleOfCellSize(cellSize.height, info.maxResizeHeight, maxAvailHeight);
            }
        }
        return new AppWidget.Size(
                minWidth, minHeight,
                width, height,
                maxWidth, maxHeight);
    }

    /**
     * Calculates the absolute minimum size of the widget.
     *
     * @param info            widget provider info, obtained via {@link AppWidgetManager#getAppWidgetInfo(int)}
     * @param includePaddings include paddings when calculating size
     * @return minimum size (in px) of the widget
     */
    private Size getMinSize(AppWidgetProviderInfo info, boolean includePaddings) {
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

    /**
     * Calculates current size of the widget.
     *
     * @param info            widget provider info, obtained via {@link AppWidgetManager#getAppWidgetInfo(int)}
     * @param options         widget options, obtained via {@link AppWidgetManager#getAppWidgetOptions(int)}
     * @param includePaddings include paddings when calculating size
     * @return size (in px) of the widget
     */
    private Size getSize(AppWidgetProviderInfo info, Bundle options, boolean includePaddings) {
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

    private static int minMultipleOfCellSize(int cellSize, int size, int max) {
        if (size <= cellSize) {
            return cellSize;
        }
        if (size % cellSize == 0) {
            return Math.min(size, max);
        }
        return Math.min(size + (cellSize - (size % cellSize)), max);
    }
}
