package com.italankin.lnch.util.imageloader.resourceloader;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    private final PackageManager packageManager;
    private final AppWidgetManager appWidgetManager;
    private final Map<ComponentName, AppWidgetProviderInfo> providers = new HashMap<>(64);

    public WidgetPreviewLoader(Context context) {
        this.context = context;
        this.appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
        this.packageManager = context.getPackageManager();
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @Nullable
    @Override
    public Drawable load(Uri uri) {
        if (providers.isEmpty()) {
            synchronized (this) {
                if (providers.isEmpty()) {
                    for (AppWidgetProviderInfo provider : appWidgetManager.getInstalledProviders()) {
                        providers.put(provider.provider, provider);
                    }
                }
            }
        }
        String packageName = uri.getAuthority();
        String className = uri.getLastPathSegment();
        AppWidgetProviderInfo info = providers.get(new ComponentName(packageName, className));
        if (info != null) {
            Drawable drawable = info.loadPreviewImage(context, Resources.getSystem().getDisplayMetrics().densityDpi);
            if (drawable != null) {
                return drawable;
            }
        }
        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
