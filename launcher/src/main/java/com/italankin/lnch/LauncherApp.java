package com.italankin.lnch;

import android.app.Application;

import com.italankin.lnch.di.service.DaggerService;
import com.italankin.lnch.util.Typefaces;

public class LauncherApp extends Application {

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        super.onCreate();
        Typefaces.init(getAssets());
        daggerService = new DaggerService(this);
    }
}
