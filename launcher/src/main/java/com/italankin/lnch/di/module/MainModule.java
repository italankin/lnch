package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.LauncherAppsRepository;
import com.italankin.lnch.model.repository.descriptor.BackupDescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.VersioningDescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.prefs.SeparatorStateImpl;
import com.italankin.lnch.model.repository.prefs.UserPreferences;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.SearchRepositoryImpl;
import com.italankin.lnch.model.repository.shortcuts.AppShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.StubShortcutsRepository;
import com.italankin.lnch.util.picasso.PicassoFactory;

import javax.inject.Singleton;

import dagger.Lazy;
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
            DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            Preferences preferences) {
        return new LauncherAppsRepository(context, packageManager, descriptorRepository,
                shortcutsRepository, preferences);
    }

    @Provides
    @Singleton
    public DescriptorRepository provideDescriptorRepository() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        return new BackupDescriptorRepository(new VersioningDescriptorRepository(gsonBuilder));
    }

    @Provides
    @Singleton
    public SearchRepository provideSearchRepository(PackageManager packageManager,
            AppsRepository appsRepository, ShortcutsRepository shortcutsRepository, Preferences preferences) {
        return new SearchRepositoryImpl(packageManager, appsRepository, shortcutsRepository, preferences);
    }

    @Provides
    @Singleton
    public PicassoFactory providePicassoFactory(Context context) {
        return new PicassoFactory(context);
    }

    @Provides
    @Singleton
    public ShortcutsRepository.DescriptorProvider provideAppsProvider(Lazy<AppsRepository> lazy) {
        return () -> lazy.get().items();
    }

    @Provides
    @Singleton
    public ShortcutsRepository provideShortcutsRepository(Context context, ShortcutsRepository.DescriptorProvider descriptorProvider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return new AppShortcutsRepository(context, descriptorProvider);
        } else {
            return new StubShortcutsRepository();
        }
    }

    @Provides
    @Singleton
    public SeparatorState provideSeparatorState(Context context) {
        return new SeparatorStateImpl(context);
    }
}
