package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.italankin.lnch.util.ResUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

public class PackageIconHandler extends RequestHandler {

    private static final String SCHEME = "package";

    public static Uri uriFrom(String packageName) {
        return new Uri.Builder().scheme(SCHEME).authority(packageName).build();
    }

    private final PackageManager packageManager;

    PackageIconHandler(Context context) {
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
            icon = packageManager.getApplicationIcon(request.uri.getAuthority());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = ResUtils.bitmapFromDrawable(icon, width, height);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
