package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.LauncherAppsRepository;
import com.italankin.lnch.model.repository.descriptors.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptors.json.GsonDescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.UserPreferences;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.SearchRepositoryImpl;

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
    public Preferences providePreferences(Context context) {
        return new UserPreferences(context);
    }

    @Provides
    @Singleton
    public AppsRepository provideAppsRepository(Context context, PackageManager packageManager,
            DescriptorRepository descriptorRepository) {
        return new LauncherAppsRepository(context, packageManager, descriptorRepository);
    }

    @Provides
    @Singleton
    public DescriptorRepository provideDescriptorRepository() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        return new GsonDescriptorRepository(gsonBuilder);
    }

    @Provides
    @Singleton
    public SearchRepository provideSearchRepository(PackageManager packageManager, AppsRepository appsRepository) {
        return new SearchRepositoryImpl(packageManager, appsRepository);
    }
}
