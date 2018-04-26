package com.italankin.lnch.di.component;

import android.content.Context;
import android.content.pm.PackageManager;

import com.italankin.lnch.di.module.AppModule;
import com.italankin.lnch.di.module.MainModule;
import com.italankin.lnch.model.repository.apps.IAppsRepository;
import com.italankin.lnch.model.repository.search.ISearchRepository;
import com.italankin.lnch.util.AppPrefs;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainModule.class, AppModule.class})
public interface MainComponent {

    Context getContext();

    PackageManager getPackageManager();

    AppPrefs getAppPrefs();

    IAppsRepository getAppsRepository();

    ISearchRepository getSearchRepository();

}
