package com.italankin.lnch.feature.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.ShortcutUtils;

import java.util.List;

@RequiresApi(Build.VERSION_CODES.N_MR1)
public class StartShortcutReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.italankin.lnch.action.START_SHORTCUT";
    private static final String EXTRA_PACKAGE_NAME = "PACKAGE_NAME";
    private static final String EXTRA_ID = "ID";

    public static Intent makeStartIntent(Shortcut shortcut) {
        Intent intent = new Intent(StartShortcutReceiver.ACTION);
        intent.putExtra(StartShortcutReceiver.EXTRA_PACKAGE_NAME, shortcut.getPackageName());
        intent.putExtra(StartShortcutReceiver.EXTRA_ID, shortcut.getId());
        intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, StartShortcutReceiver.class.getName()));
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) {
            return;
        }
        String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        String shortcutId = intent.getStringExtra(EXTRA_ID);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(shortcutId)) {
            return;
        }
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        //noinspection ConstantConditions
        if (!launcherApps.hasShortcutHostPermission()) {
            return;
        }
        List<ShortcutInfo> shortcuts = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        if (shortcuts == null || shortcuts.isEmpty()) {
            return;
        }
        launcherApps.startShortcut(shortcuts.get(0), null, null);
    }
}
