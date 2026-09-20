package com.italankin.lnch.feature.lock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.DialogUtils;
import com.italankin.lnch.util.IntentUtils;

public final class ScreenLock {

    public static void lock(Context context) {
        if (!isAvailable() || !isEnabled()) {
            return;
        }
        if (ScreenLockService.isConnected()) {
            if (!ScreenLockService.lockScreen()) {
                Toast.makeText(context, R.string.screen_lock_failed, Toast.LENGTH_SHORT).show();
            }
        }
        // no permission?
    }

    public static boolean isAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static boolean isEnabled() {
        return LauncherApp.daggerService.main().preferences().get(Preferences.DOUBLE_TAP_TO_LOCK);
    }

    public static boolean isAccessibilityEnabled(Context context) {
        if (!isAvailable()) {
            return false;
        }
        String services = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (services == null) {
            return false;
        }
        ComponentName component = new ComponentName(context, ScreenLockService.class);
        for (String service : services.split(":")) {
            if (component.equals(ComponentName.unflattenFromString(service))) {
                return true;
            }
        }
        return false;
    }

    public static void requestConsent(Fragment fragment, Runnable onConsent) {
        if (!isAvailable()) {
            return;
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(R.string.settings_home_screen_lock_consent_title)
                .setMessage(R.string.settings_home_screen_lock_consent_message)
                .setPositiveButton(R.string.settings_home_screen_lock_consent_accept, (d, which) -> {
                    onConsent.run();
                })
                .setNegativeButton(R.string.settings_home_screen_lock_consent_decline, null)
                .show();
        DialogUtils.dismissOnDestroy(fragment.getViewLifecycleOwner(), dialog);
    }

    public static void openAccessibilitySettings(Context context) {
        if (!IntentUtils.safeStartActivity(context, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
            Toast.makeText(context, R.string.settings_home_screen_lock_settings_failed, Toast.LENGTH_LONG).show();
        }
    }

    private ScreenLock() {
    }
}
