package com.italankin.lnch.di.component;

import android.content.Context;

import com.italankin.lnch.di.module.BackupModule;
import com.italankin.lnch.di.module.MainModule;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.picasso.PicassoFactory;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {MainModule.class, BackupModule.class})
public interface MainComponent extends PresenterComponent.Dependencies {

    Preferences preferences();

    DescriptorRepository descriptorRepository();

    SearchRepository searchRepository();

    PicassoFactory picassoFactory();

    ShortcutsRepository shortcutsRepository();

    NameNormalizer nameNormalizer();

    IntentQueue intentQueue();

    NotificationsRepository notificationsRepository();

    HomeDescriptorsState homeDescriptorState();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder context(Context context);

        MainComponent build();
    }
}
