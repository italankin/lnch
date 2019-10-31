package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.italankin.lnch.util.ResUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import timber.log.Timber;

public class PackageResourceHandler extends RequestHandler {

    private static final String SCHEME = "package.resource";

    public static Uri uriFrom(String packageName, int id) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendPath(String.valueOf(id))
                .build();
    }

    private final PackageManager packageManager;

    PackageResourceHandler(Context context) {
        packageManager = context.getPackageManager();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && SCHEME.equals(data.uri.getScheme());
    }

    @Nullable
    @Override
    public Result load(Request request, int networkPolicy) {
        Drawable icon;
        try {
            String packageName = request.uri.getAuthority();
            String segment = request.uri.getLastPathSegment();
            if (segment == null) {
                return null;
            }
            int id = Integer.parseInt(segment);
            if (id == 0) {
                return null;
            }
            icon = packageManager
                    .getResourcesForApplication(packageName)
                    .getDrawableForDensity(id, Resources.getSystem().getDisplayMetrics().densityDpi);
            if (icon == null) {
                return null;
            }
        } catch (Exception e) {
            Timber.e(e, "load:");
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = ResUtils.bitmapFromDrawable(icon, width, height);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
