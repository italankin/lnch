package com.italankin.lnch.di.component;

import android.content.Context;

import com.italankin.lnch.di.module.AppModule;
import com.italankin.lnch.di.module.BackupModule;
import com.italankin.lnch.di.module.MainModule;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.settings.backup.impl.BackupReader;
import com.italankin.lnch.feature.settings.backup.impl.BackupWriter;
import com.italankin.lnch.feature.settings.backup.impl.PreferencesBackup;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.prefs.WidgetsState;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.picasso.PicassoFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainModule.class, AppModule.class, BackupModule.class})
public interface MainComponent {

    Context getContext();

    Preferences getPreferences();

    DescriptorRepository getDescriptorRepository();

    SearchRepository getSearchRepository();

    PicassoFactory getPicassoFactory();

    ShortcutsRepository getShortcutsRepository();

    SeparatorState getSeparatorState();

    NameNormalizer getNameNormalizer();

    IntentQueue getIntentQueue();

    WidgetsState getWidgetsState();

    NotificationsRepository getNotificationsRepository();

    BackupReader getBackupReader();

    BackupWriter getBackupWriter();

    PreferencesBackup getPreferencesBackup();
}
