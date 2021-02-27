package com.italankin.lnch.di.module;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.model.backup.BackupReader;
import com.italankin.lnch.model.backup.BackupReaderImpl;
import com.italankin.lnch.model.backup.BackupWriter;
import com.italankin.lnch.model.backup.BackupWriterImpl;
import com.italankin.lnch.model.backup.PreferencesBackup;
import com.italankin.lnch.model.backup.PreferencesBackupImpl;
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
