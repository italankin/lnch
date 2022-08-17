package com.italankin.lnch.util.imageloader.resourceloader;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.ShortcutUtils;
import com.italankin.lnch.util.icons.BadgedIconDrawable;

import java.util.List;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutIconLoader implements ResourceLoader {

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

    public ShortcutIconLoader(Context context) {
        launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        packageManager = context.getPackageManager();
    }

    @Override
    public boolean handles(Uri uri) {
        return SCHEME.equals(uri.getScheme());
    }

    @Override
    public Drawable load(Uri uri) {
        String packageName = uri.getAuthority();
        String shortcutId = uri.getQueryParameter(ID);
        List<ShortcutInfo> shortcuts = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        if (shortcuts.isEmpty()) {
            return null;
        }
        Drawable icon = launcherApps.getShortcutIconDrawable(shortcuts.get(0),
                Resources.getSystem().getDisplayMetrics().densityDpi);
        if (icon == null) {
            return null;
        }
        if ("true".equals(uri.getQueryParameter(BADGED))) {
            try {
                return new BadgedIconDrawable(icon, packageManager.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return icon;
    }
}
