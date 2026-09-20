package com.italankin.lnch.feature.lock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.P)
public class ScreenLockService extends AccessibilityService {

    private static ScreenLockService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    static boolean isConnected() {
        return instance != null;
    }

    static boolean lockScreen() {
        return ScreenLock.isEnabled() && instance != null && instance.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        clearInstance();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        clearInstance();
        super.onDestroy();
    }

    private void clearInstance() {
        if (instance == this) {
            instance = null;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Screen locking does not need accessibility events or window content.
    }

    @Override
    public void onInterrupt() {
    }
}
