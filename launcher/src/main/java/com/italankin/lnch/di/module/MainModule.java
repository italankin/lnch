package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.repository.apps.IAppsRepository;
import com.italankin.lnch.model.repository.apps.LauncherAppsRepository;
import com.italankin.lnch.model.repository.search.ISearchRepository;
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
    public IAppsRepository provideAppsRepository(Context context) {
        return new LauncherAppsRepository(context);
    }

    @Provides
    @Singleton
    public ISearchRepository provideSearchRepository(Context context, IAppsRepository appsRepository) {
        return new SearchRepositoryImpl(context, appsRepository);
    }
}
