package com.italankin.lnch.di.component;

import android.content.Context;
import android.content.pm.PackageManager;

import com.italankin.lnch.di.module.AppModule;
import com.italankin.lnch.di.module.MainModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainModule.class, AppModule.class})
public interface MainComponent {

    Context getContext();

    PackageManager getPackageManager();

}
