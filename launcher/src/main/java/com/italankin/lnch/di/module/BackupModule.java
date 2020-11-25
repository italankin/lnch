package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.feature.settings.backup.impl.BackupReader;
import com.italankin.lnch.feature.settings.backup.impl.BackupReaderImpl;
import com.italankin.lnch.feature.settings.backup.impl.BackupWriter;
import com.italankin.lnch.feature.settings.backup.impl.BackupWriterImpl;
import com.italankin.lnch.feature.settings.backup.impl.PreferencesBackup;
import com.italankin.lnch.feature.settings.backup.impl.PreferencesBackupImpl;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;

import dagger.Module;
import dagger.Provides;

@Module
public class BackupModule {

    @Provides
    BackupReader provideBackupReader(Context context,
            GsonBuilder gsonBuilder,
            DescriptorRepository descriptorRepository,
            DescriptorStore descriptorStore,
            PreferencesBackup preferencesBackup) {
        return new BackupReaderImpl(context, gsonBuilder, descriptorRepository, descriptorStore, preferencesBackup);
    }

    @Provides
    BackupWriter provideBackupWriter(Context context,
            GsonBuilder gsonBuilder,
            DescriptorRepository descriptorRepository,
            PreferencesBackup preferencesBackup) {
        return new BackupWriterImpl(context, gsonBuilder, descriptorRepository, preferencesBackup);
    }

    @Provides
    PreferencesBackup providePreferencesBackup(SharedPreferences sharedPreferences) {
        return new PreferencesBackupImpl(sharedPreferences);
    }
}
