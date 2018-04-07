package com.italankin.lnch;

import android.app.Application;

import com.italankin.lnch.di.service.DaggerService;

public class App extends Application {

    public DaggerService daggerService;

    @Override
    public void onCreate() {
        super.onCreate();
        daggerService = new DaggerService(this);
    }
}
