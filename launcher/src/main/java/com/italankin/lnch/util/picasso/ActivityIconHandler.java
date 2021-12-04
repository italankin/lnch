package com.italankin.lnch.util.picasso;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.italankin.lnch.util.ResUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import androidx.annotation.Nullable;

public class ActivityIconHandler extends RequestHandler {

    private static final String SCHEME = "activity";

    public static Uri uriFrom(String packageName, String className) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendEncodedPath(className)
                .build();
    }

    private final PackageManager packageManager;

    ActivityIconHandler(Context context) {
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
            String className = request.uri.getLastPathSegment();
            icon = packageManager.getActivityIcon(new ComponentName(packageName, className));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = ResUtils.bitmapFromDrawable(icon, width, height);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
