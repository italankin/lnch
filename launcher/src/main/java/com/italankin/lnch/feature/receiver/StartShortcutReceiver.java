package com.italankin.lnch.feature.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.api.LauncherIntents;
import com.italankin.lnch.util.ShortcutUtils;

import java.util.List;

import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.N_MR1)
public class StartShortcutReceiver extends BroadcastReceiver {

    public static Intent makeStartIntent(Shortcut shortcut) {
        Intent intent = new Intent(LauncherIntents.ACTION_START_SHORTCUT);
        intent.putExtra(LauncherIntents.EXTRA_PACKAGE_NAME, shortcut.getPackageName());
        intent.putExtra(LauncherIntents.EXTRA_SHORTCUT_ID, shortcut.getId());
        intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, StartShortcutReceiver.class.getName()));
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LauncherIntents.ACTION_START_SHORTCUT.equals(intent.getAction())) {
            return;
        }
        String packageName = intent.getStringExtra(LauncherIntents.EXTRA_PACKAGE_NAME);
        String shortcutId = intent.getStringExtra(LauncherIntents.EXTRA_SHORTCUT_ID);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(shortcutId)) {
            return;
        }
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (!launcherApps.hasShortcutHostPermission()) {
            return;
        }
        List<ShortcutInfo> shortcuts = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        if (shortcuts.isEmpty()) {
            return;
        }
        ShortcutInfo shortcut = shortcuts.get(0);
        if (!shortcut.isEnabled()) {
            return;
        }
        try {
            launcherApps.startShortcut(shortcut, null, null);
        } catch (Exception e) {
            Timber.e(e, "onReceive:");
        }
    }
}
