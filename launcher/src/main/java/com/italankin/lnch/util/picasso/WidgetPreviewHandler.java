package com.italankin.lnch.util.picasso;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.italankin.lnch.util.ResUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import androidx.annotation.Nullable;

public class WidgetPreviewHandler extends RequestHandler {

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

    WidgetPreviewHandler(Context context) {
        this.context = context;
        this.appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && SCHEME.equals(data.uri.getScheme());
    }

    @Nullable
    @Override
    public Result load(Request request, int networkPolicy) {
        String packageName = request.uri.getAuthority();
        String className = request.uri.getLastPathSegment();
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
        Drawable icon = info.loadPreviewImage(context, Resources.getSystem().getDisplayMetrics().densityDpi);
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = ResUtils.bitmapFromDrawable(icon, width, height);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
