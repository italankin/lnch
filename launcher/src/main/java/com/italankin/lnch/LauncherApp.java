package com.italankin.lnch;

import android.app.Application;
import com.italankin.lnch.di.service.DaggerService;
import com.italankin.lnch.util.ThemeActivityCallbacks;
import timber.log.Timber;

public class LauncherApp extends Application {

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        super.onCreate();
        daggerService = new DaggerService(this);
        registerActivityLifecycleCallbacks(new ThemeActivityCallbacks());
    }
}
