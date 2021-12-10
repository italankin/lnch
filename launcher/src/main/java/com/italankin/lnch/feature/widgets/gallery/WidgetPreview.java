package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.italankin.lnch.util.picasso.WidgetPreviewHandler;

class WidgetPreview {

    final AppWidgetProviderInfo info;
    final Uri previewUri;
    final Uri iconUri;
    final String label;
    final String appName;

    WidgetPreview(PackageManager packageManager, AppWidgetProviderInfo info) {
        this.info = info;
        this.previewUri = WidgetPreviewHandler.uriFrom(info.provider);
        String packageName = info.provider.getPackageName();
        this.iconUri = PackageIconHandler.uriFrom(packageName);
        this.label = info.loadLabel(packageManager);
        CharSequence label = PackageUtils.getPackageLabel(packageManager, packageName);
        this.appName = label != null ? label.toString() : null;
    }
}
