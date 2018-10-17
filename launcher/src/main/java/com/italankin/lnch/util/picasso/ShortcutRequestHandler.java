package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.util.Collections;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutRequestHandler extends RequestHandler {

    private static final String SCHEME = "shortcut";
    private static final String ID = "id";

    public static Uri uriFrom(String packageName, String shortcutId) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(packageName)
                .appendQueryParameter(ID, shortcutId)
                .build();
    }

    private final LauncherApps launcherApps;

    ShortcutRequestHandler(Context context) {
        launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && SCHEME.equals(data.uri.getScheme());
    }

    @Nullable
    @Override
    public Result load(Request request, int networkPolicy) {
        ShortcutQuery query = new ShortcutQuery();
        query.setPackage(request.uri.getAuthority());
        String shortcutId = request.uri.getQueryParameter(ID);
        query.setShortcutIds(Collections.singletonList(shortcutId));
        query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_DYNAMIC);
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        if (shortcuts == null || shortcuts.isEmpty()) {
            return null;
        }
        Drawable icon = launcherApps.getShortcutIconDrawable(shortcuts.get(0), 0);
        if (icon == null) {
            return null;
        }
        int width = request.targetWidth;
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        int height = request.targetHeight;
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
