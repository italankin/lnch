package com.italankin.lnch;

import timber.log.Timber;

public class DebugLauncherApp extends LauncherApp {
    @Override
    public void onCreate() {
        Timber.plant(new Timber.DebugTree());
        super.onCreate();
    }
}
