package com.italankin.lnch.feature.widgets.model;

import android.appwidget.AppWidgetProviderInfo;
import android.os.Bundle;

public class AppWidget {

    public final int appWidgetId;
    public final AppWidgetProviderInfo providerInfo;
    public final Bundle options;
    public final Size size;
    public boolean resizeMode = false;
    public boolean forceResize = false;

    public AppWidget(int appWidgetId,
            AppWidgetProviderInfo providerInfo,
            Bundle options,
            Size size) {
        this.appWidgetId = appWidgetId;
        this.providerInfo = providerInfo;
        this.options = options;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppWidget appWidget = (AppWidget) o;
        return appWidgetId == appWidget.appWidgetId;
    }

    @Override
    public int hashCode() {
        return appWidgetId;
    }

    public static class Size {

        public final int minWidth;
        public final int minHeight;
        public int width;
        public int height;
        public final int maxWidth;
        public final int maxHeight;

        public Size(int width, int height) {
            minWidth = maxWidth = this.width = width;
            minHeight = maxHeight = this.height = height;
        }

        public Size(int minWidth, int minHeight, int width, int height, int maxWidth, int maxHeight) {
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            this.width = width;
            this.height = height;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }
    }
}
