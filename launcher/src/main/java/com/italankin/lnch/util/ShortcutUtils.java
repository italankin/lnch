package com.italankin.lnch.util;

import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Process;

import java.util.Collections;
import java.util.List;

import androidx.annotation.RequiresApi;

public final class ShortcutUtils {

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static List<ShortcutInfo> findById(LauncherApps launcherApps, String packageName, String shortcutId) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        ShortcutQuery query = new ShortcutQuery();
        query.setPackage(packageName);
        query.setShortcutIds(Collections.singletonList(shortcutId));
        query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_DYNAMIC
                | ShortcutQuery.FLAG_MATCH_PINNED);
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        if (shortcuts == null || shortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        return shortcuts;
    }

    private ShortcutUtils() {
        // no instance
    }
}
