package com.italankin.lnch;

import android.app.Application;

import com.italankin.lnch.di.service.DaggerService;

public class LauncherApp extends Application {

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        super.onCreate();
        daggerService = new DaggerService(this);
    }
}
