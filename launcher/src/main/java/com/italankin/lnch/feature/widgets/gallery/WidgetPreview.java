package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.imageloader.resourceloader.PackageIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.WidgetPreviewLoader;
import com.italankin.lnch.util.search.Searchable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class WidgetPreview implements Searchable {

    final AppWidgetProviderInfo info;
    final Uri previewUri;
    final Uri iconUri;
    final String label;
    final String appName;
    private final Set<String> searchTokens = new HashSet<>(4);

    WidgetPreview(PackageManager packageManager, AppWidgetProviderInfo info) {
        this.info = info;
        this.previewUri = WidgetPreviewLoader.uriFrom(info.provider);
        String packageName = info.provider.getPackageName();
        this.iconUri = PackageIconLoader.uriFrom(packageName);
        this.label = info.loadLabel(packageManager);
        CharSequence label = PackageUtils.getPackageLabel(packageManager, packageName);
        this.appName = label != null ? label.toString() : null;

        Collections.addAll(searchTokens, appName, this.label);
    }

    @Override
    public Set<String> getSearchTokens() {
        return searchTokens;
    }
}
