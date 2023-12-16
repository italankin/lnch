package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.italankin.lnch.util.imageloader.resourceloader.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

public class WidgetPreviewLoader implements ResourceLoader {

    private static final String SCHEME = "widget.preview";
    private static final String PARAM_ICON = "icon";

    public static Uri uriFrom(ComponentName provider, boolean icon) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(provider.getPackageName())
                .appendEncodedPath(provider.getClassName())
                .appendQueryParameter(PARAM_ICON, String.valueOf(icon))
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
            int densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
            Drawable drawable;
            if ("true".equals(uri.getQueryParameter(PARAM_ICON))) {
                drawable = info.loadIcon(context, densityDpi);
            } else {
                drawable = info.loadPreviewImage(context, densityDpi);
            }
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
