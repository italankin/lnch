package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import com.google.gson.GsonBuilder;
import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsStateImpl;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.settings.searchstore.SettingsStore;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.apps.LauncherDescriptorRepository;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.notifications.NotificationsRepositoryImpl;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.UserPreferences;
import com.italankin.lnch.model.repository.prefs.WidgetsState;
import com.italankin.lnch.model.repository.prefs.WidgetsStateImpl;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.SearchRepositoryImpl;
import com.italankin.lnch.model.repository.search.delegate.*;
import com.italankin.lnch.model.repository.search.preference.PreferenceSearchDelegate;
import com.italankin.lnch.model.repository.shortcuts.AppShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.shortcuts.backport.BackportShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.model.repository.store.json.DescriptorJsonTypeAdapter;
import com.italankin.lnch.model.repository.store.json.GsonDescriptorStore;
import com.italankin.lnch.model.repository.store.json.JsonPackagesStore;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.repository.usage.UsageTrackerImpl;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Module
public class MainModule {

    @Provides
    @Singleton
    PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    @Provides
    @Singleton
    Preferences providePreferences(SharedPreferences sharedPreferences) {
        return new UserPreferences(sharedPreferences);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    DescriptorRepository provideDescriptorRepository(Context context, PackageManager packageManager,
            DescriptorStore descriptorStore, PackagesStore packagesStore,
            ShortcutsRepository shortcutsRepository, Preferences preferences,
            NameNormalizer nameNormalizer) {
        return new LauncherDescriptorRepository(context, packageManager, descriptorStore,
                packagesStore, shortcutsRepository, preferences, nameNormalizer);
    }

    @Provides
    @Singleton
    PackagesStore providePackagesStore(Context context) {
        return new JsonPackagesStore(context.getFilesDir());
    }

    @Provides
    GsonBuilder provideGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        gsonBuilder.registerTypeAdapter(Descriptor.class, new DescriptorJsonTypeAdapter());
        return gsonBuilder;
    }

    @Provides
    @Singleton
    DescriptorStore provideDescriptorStore(GsonBuilder gsonBuilder) {
        return new GsonDescriptorStore(gsonBuilder);
    }

    @Provides
    @Singleton
    FontManager provideFontManager(Context context) {
        return new FontManager(context);
    }

    @Provides
    @Singleton
    SearchRepository provideSearchRepository(PackageManager packageManager,
            DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            Preferences preferences, SettingsStore settingsStore, UsageTracker usageTracker) {
        List<SearchDelegate> delegates = Arrays.asList(
                new AppSearchDelegate(packageManager, descriptorRepository),
                new DeepShortcutSearchDelegate(descriptorRepository, shortcutsRepository),
                new PinnedShortcutSearchDelegate(descriptorRepository),
                new IntentSearchDelegate(descriptorRepository),
                new PreferenceSearchDelegate(settingsStore)
        );
        List<SearchDelegate> additionalDelegates = Arrays.asList(
                new WebSearchDelegate(preferences),
                new UrlSearchDelegate()
        );
        return new SearchRepositoryImpl(packageManager, delegates, additionalDelegates, preferences, usageTracker,
                descriptorRepository, shortcutsRepository);
    }

    @Provides
    @Singleton
    ShortcutsRepository provideShortcutsRepository(Context context, Lazy<DescriptorRepository> descriptorRepository,
            NameNormalizer nameNormalizer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return new AppShortcutsRepository(context, descriptorRepository, nameNormalizer);
        } else {
            return new BackportShortcutsRepository(context, descriptorRepository, nameNormalizer);
        }
    }

    @Provides
    @Singleton
    NameNormalizer provideNameNormalizer(Preferences preferences) {
        return new NameNormalizer(preferences);
    }

    @Provides
    @Singleton
    IntentQueue provideIntentQueue() {
        return new IntentQueue();
    }

    @Provides
    @Singleton
    WidgetsState provideWidgetsState(Context context, GsonBuilder gsonBuilder) {
        return new WidgetsStateImpl(context, gsonBuilder.create());
    }

    @Provides
    @Singleton
    NotificationsRepository provideNotificationsRepository(DescriptorRepository descriptorRepository) {
        return new NotificationsRepositoryImpl(descriptorRepository);
    }

    @Provides
    @Singleton
    HomeDescriptorsState provideHomeDescriptorsState() {
        return new HomeDescriptorsStateImpl();
    }

    @Provides
    @Singleton
    UsageTracker provideUsageTracker(Context context, DescriptorRepository descriptorRepository,
            GsonBuilder gsonBuilder) {
        return new UsageTrackerImpl(context, descriptorRepository, gsonBuilder.create());
    }

    @Provides
    @Singleton
    SettingsStore provideSettingsStore(Context context) {
        return new SettingsStore(context);
    }
}
