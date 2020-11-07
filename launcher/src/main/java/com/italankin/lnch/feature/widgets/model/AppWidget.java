package com.italankin.lnch.feature.widgets.model;

import android.appwidget.AppWidgetProviderInfo;
import android.os.Bundle;

public class AppWidget implements WidgetAdapterItem {

    public final int appWidgetId;
    public final AppWidgetProviderInfo providerInfo;
    public final Bundle options;
    public final int minWidth, minHeight, maxWidth, maxHeight;

    public AppWidget(int appWidgetId,
            AppWidgetProviderInfo providerInfo,
            Bundle options,
            int minWidth,
            int minHeight,
            int maxWidth,
            int maxHeight) {
        this.appWidgetId = appWidgetId;
        this.providerInfo = providerInfo;
        this.options = options;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
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

        if (appWidgetId != appWidget.appWidgetId) {
            return false;
        }
        if (minWidth != appWidget.minWidth) {
            return false;
        }
        if (minHeight != appWidget.minHeight) {
            return false;
        }
        if (maxWidth != appWidget.maxWidth) {
            return false;
        }
        if (maxHeight != appWidget.maxHeight) {
            return false;
        }
        if (!options.equals(appWidget.options)) {
            return false;
        }
        return providerInfo.provider.equals(appWidget.providerInfo.provider);
    }

    @Override
    public int hashCode() {
        int result = appWidgetId;
        result = 31 * result + providerInfo.provider.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + minWidth;
        result = 31 * result + minHeight;
        result = 31 * result + maxWidth;
        result = 31 * result + maxHeight;
        return result;
    }
}
