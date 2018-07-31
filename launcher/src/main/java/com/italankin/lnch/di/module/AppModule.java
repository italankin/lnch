package com.italankin.lnch.di.module;

import android.content.Context;

import com.italankin.lnch.LauncherApp;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final LauncherApp launcherApp;

    public AppModule(LauncherApp launcherApp) {
        this.launcherApp = launcherApp;
    }

    @Singleton
    @Provides
    public Context provideContext() {
        return launcherApp;
    }
}
