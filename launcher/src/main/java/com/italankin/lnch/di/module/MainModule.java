package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.LauncherAppsRepository;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.SearchRepositoryImpl;
import com.italankin.lnch.util.AppPrefs;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    @Provides
    @Singleton
    public PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    @Provides
    @Singleton
    public AppPrefs provideAppPrefs(Context context) {
        return new AppPrefs(context);
    }

    @Provides
    @Singleton
    public AppsRepository provideAppsRepository(Context context) {
        return new LauncherAppsRepository(context);
    }

    @Provides
    @Singleton
    public SearchRepository provideSearchRepository(Context context, AppsRepository appsRepository) {
        return new SearchRepositoryImpl(context, appsRepository);
    }
}
