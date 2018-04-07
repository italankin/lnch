package com.italankin.lnch.di.module;

import android.content.Context;

import com.italankin.lnch.App;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Singleton
    @Provides
    public Context provideContext() {
        return app;
    }
}
