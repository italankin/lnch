package com.italankin.lnch;

import android.app.Application;
import android.content.Context;

import com.italankin.lnch.di.service.DaggerService;

public class LauncherApp extends Application {

    public static LauncherApp getInstance(Context context) {
        return (LauncherApp) context.getApplicationContext();
    }

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        super.onCreate();
        daggerService = new DaggerService(this);
    }
}
