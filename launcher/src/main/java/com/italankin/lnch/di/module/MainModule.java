package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.impl.LauncherDescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.prefs.SeparatorStateImpl;
import com.italankin.lnch.model.repository.prefs.UserPreferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.SearchRepositoryImpl;
import com.italankin.lnch.model.repository.search.delegate.AppSearchDelegate;
import com.italankin.lnch.model.repository.search.delegate.DeepShortcutSearchDelegate;
import com.italankin.lnch.model.repository.search.delegate.IntentSearchDelegate;
import com.italankin.lnch.model.repository.search.delegate.PinnedShortcutSearchDelegate;
import com.italankin.lnch.model.repository.shortcuts.AppShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.StubShortcutsRepository;
import com.italankin.lnch.model.repository.store.BackupDescriptorStore;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.model.repository.store.json.GsonDescriptorStore;
import com.italankin.lnch.model.repository.store.json.JsonPackagesStore;
import com.italankin.lnch.util.picasso.PicassoFactory;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    @Provides
    @Singleton
    PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    @Provides
    @Singleton
    Preferences providePreferences(Context context) {
        return new UserPreferences(context);
    }

    @Provides
    @Singleton
    DescriptorRepository provideDescriptorRepository(Context context, PackageManager packageManager,
            DescriptorStore descriptorStore, PackagesStore packagesStore, ShortcutsRepository shortcutsRepository,
            Preferences preferences) {
        return new LauncherDescriptorRepository(context, packageManager, descriptorStore,
                packagesStore, shortcutsRepository, preferences);
    }

    @Provides
    @Singleton
    PackagesStore providerPackagesStore(Context context) {
        return new JsonPackagesStore(context.getFilesDir());
    }

    @Provides
    @Singleton
    DescriptorStore provideDescriptorStore(PackagesStore packagesStore, Context context) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        return new BackupDescriptorStore(new GsonDescriptorStore(gsonBuilder),
                packagesStore, context.getFilesDir());
    }

    @Provides
    @Singleton
    SearchRepository provideSearchRepository(PackageManager packageManager,
            DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            Preferences preferences) {
        List<SearchDelegate> delegates = Arrays.asList(
                new AppSearchDelegate(packageManager, descriptorRepository),
                new DeepShortcutSearchDelegate(descriptorRepository, shortcutsRepository),
                new PinnedShortcutSearchDelegate(descriptorRepository),
                new IntentSearchDelegate(descriptorRepository)
        );
        return new SearchRepositoryImpl(delegates, preferences);
    }

    @Provides
    @Singleton
    PicassoFactory providePicassoFactory(Context context) {
        return new PicassoFactory(context);
    }

    @Provides
    @Singleton
    ShortcutsRepository.DescriptorProvider provideAppsProvider(Lazy<DescriptorRepository> lazy) {
        return () -> lazy.get().items();
    }

    @Provides
    @Singleton
    ShortcutsRepository provideShortcutsRepository(Context context, ShortcutsRepository.DescriptorProvider descriptorProvider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return new AppShortcutsRepository(context, descriptorProvider);
        } else {
            return new StubShortcutsRepository();
        }
    }

    @Provides
    @Singleton
    SeparatorState provideSeparatorState(Context context) {
        return new SeparatorStateImpl(context);
    }
}
