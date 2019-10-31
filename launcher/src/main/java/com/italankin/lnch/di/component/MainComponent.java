package com.italankin.lnch.di.component;

import android.content.Context;

import com.italankin.lnch.di.module.AppModule;
import com.italankin.lnch.di.module.MainModule;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.picasso.PicassoFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainModule.class, AppModule.class})
public interface MainComponent {

    Context getContext();

    Preferences getPreferences();

    DescriptorRepository getDescriptorRepository();

    SearchRepository getSearchRepository();

    PicassoFactory getPicassoFactory();

    ShortcutsRepository getShortcutsRepository();

    SeparatorState getSeparatorState();

    DescriptorStore getDescriptorStore();

    PackagesStore getPackagesStore();

    NameNormalizer getNameNormalizer();
}
