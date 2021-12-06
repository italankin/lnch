package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.italankin.lnch.util.picasso.WidgetPreviewHandler;

class WidgetPreview implements WidgetGalleryItem {

    final AppWidgetProviderInfo info;
    final Uri previewUri;
    final Uri iconUri;
    final CharSequence label;

    WidgetPreview(PackageManager packageManager, AppWidgetProviderInfo info) {
        this.info = info;
        this.previewUri = WidgetPreviewHandler.uriFrom(info.provider);
        this.iconUri = PackageIconHandler.uriFrom(info.provider.getPackageName());
        this.label = info.loadLabel(packageManager);
    }
}
