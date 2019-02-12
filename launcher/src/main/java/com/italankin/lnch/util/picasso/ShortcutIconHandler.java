package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.ShortcutUtils;
import com.italankin.lnch.util.icons.BadgedIconDrawable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutIconHandler extends RequestHandler {

    private static final String SCHEME = "shortcut";
    private static final String ID = "id";
    private static final String BADGED = "badged";

    public static Uri uriFrom(Shortcut shortcut) {
        return uriFrom(shortcut.getPackageName(), shortcut.getId());
    }

    public static Uri uriFrom(Shortcut shortcut, boolean badged) {
        return uriFrom(shortcut.getPackageName(), shortcut.getId(), badged);
    }

    public static Uri uriFrom(String packageName, String shortcutId) {
        return uriFrom(packageName, shortcutId, false);
    }

    public static Uri uriFrom(String packageName, String shortcutId, boolean badged) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendQueryParameter(ID, shortcutId)
                .appendQueryParameter(BADGED, String.valueOf(badged))
                .build();
    }

    private final LauncherApps launcherApps;
    private final PackageManager packageManager;

    ShortcutIconHandler(Context context) {
        launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        packageManager = context.getPackageManager();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && SCHEME.equals(data.uri.getScheme());
    }

    @Nullable
    @Override
    public Result load(Request request, int networkPolicy) {
        String packageName = request.uri.getAuthority();
        String shortcutId = request.uri.getQueryParameter(ID);
        List<ShortcutInfo> shortcuts = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        if (shortcuts.isEmpty()) {
            return null;
        }
        Drawable icon = launcherApps.getShortcutIconDrawable(shortcuts.get(0),
                Resources.getSystem().getDisplayMetrics().densityDpi);
        if (icon == null) {
            return null;
        }
        float ratio = icon.getIntrinsicWidth() / (float) icon.getIntrinsicHeight();
        int width = Math.max(icon.getIntrinsicWidth(), request.targetWidth);
        int height = (int) (width / ratio);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        if ("true".equals(request.uri.getQueryParameter(BADGED))) {
            try {
                icon = new BadgedIconDrawable(icon, packageManager.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
