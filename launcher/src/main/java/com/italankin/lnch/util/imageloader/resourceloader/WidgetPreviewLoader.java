package com.italankin.lnch.util.imageloader.resourceloader;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class WidgetPreviewLoader implements ResourceLoader {

    private static final String SCHEME = "widget.preview";

    public static Uri uriFrom(ComponentName provider) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(provider.getPackageName())
                .appendEncodedPath(provider.getClassName())
                .build();
    }

    private final Context context;
    private final AppWidgetManager appWidgetManager;

    public WidgetPreviewLoader(Context context) {
        this.context = context;
        this.appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @Override
    public Drawable load(Uri uri) {
        String packageName = uri.getAuthority();
        String className = uri.getLastPathSegment();
        AppWidgetProviderInfo info = null;
        for (AppWidgetProviderInfo provider : appWidgetManager.getInstalledProviders()) {
            if (provider.provider.getPackageName().equals(packageName) &&
                    provider.provider.getClassName().equals(className)) {
                info = provider;
                break;
            }
        }
        if (info == null) {
            return null;
        }
        return info.loadPreviewImage(context, Resources.getSystem().getDisplayMetrics().densityDpi);
    }
}
