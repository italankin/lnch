package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.Set;

class WidgetPreview implements Searchable {

    final AppWidgetProviderInfo info;
    final Uri previewUri;
    final Uri iconUri;
    final String label;
    final String description;
    final String appName;
    private final Set<String> searchTokens;

    WidgetPreview(Context context, PackageManager packageManager, AppWidgetProviderInfo info) {
        this.info = info;
        this.previewUri = WidgetPreviewLoader.uriFrom(info.provider, false);
        this.iconUri = WidgetPreviewLoader.uriFrom(info.provider, true);
        this.label = info.loadLabel(packageManager);
        CharSequence packageLabel = PackageUtils.getPackageLabel(packageManager, info.provider.getPackageName());
        this.appName = packageLabel != null ? packageLabel.toString() : null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CharSequence desc = info.loadDescription(context);
            if (!TextUtils.isEmpty(desc)) {
                description = desc.toString();
            } else {
                description = null;
            }
        } else {
            description = null;
        }
        searchTokens = Searchable.createTokens(appName, label, description);
    }

    @Override
    public Set<String> getSearchTokens() {
        return searchTokens;
    }
}
