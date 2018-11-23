package com.italankin.lnch;

import android.os.StrictMode;

import timber.log.Timber;

public class DebugLauncherApp extends LauncherApp {
    @Override
    public void onCreate() {
        if (false) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedClosableObjects()
                    .build());
        }
        Timber.plant(new Timber.DebugTree());
        super.onCreate();
    }
}
