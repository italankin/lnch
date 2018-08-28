package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

public class PackageManagerRequestHandler extends RequestHandler {

    public static final String SCHEME = "pm";

    public static Uri uriFrom(String packageName) {
        return new Uri.Builder().scheme(SCHEME).authority(packageName).build();
    }

    private final PackageManager packageManager;

    public PackageManagerRequestHandler(Context context) {
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
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
