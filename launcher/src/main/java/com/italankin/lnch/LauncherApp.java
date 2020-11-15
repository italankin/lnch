package com.italankin.lnch;

import android.app.Application;

import com.italankin.lnch.di.service.DaggerService;
import com.italankin.lnch.util.ThemeActivityCallbacks;

public class LauncherApp extends Application {

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        super.onCreate();
        daggerService = new DaggerService(this);
        registerActivityLifecycleCallbacks(new ThemeActivityCallbacks());
    }
}
