package com.italankin.lnch.di.component;

import android.content.Context;
import com.italankin.lnch.di.scope.ViewModelScope;
import com.italankin.lnch.feature.home.apps.AppsPresenter;
import com.italankin.lnch.feature.home.apps.folder.EditFolderViewModel;
import com.italankin.lnch.feature.home.apps.folder.FolderViewModel;
import com.italankin.lnch.feature.intentfactory.componentselector.ComponentSelectorViewModel;
import com.italankin.lnch.feature.settings.apps.AppsSettingsViewModel;
import com.italankin.lnch.feature.settings.apps.details.AppDetailsViewModel;
import com.italankin.lnch.feature.settings.apps.details.aliases.AppAliasesViewModel;
import com.italankin.lnch.feature.settings.backup.BackupPresenter;
import com.italankin.lnch.feature.settings.fonts.FontsPresenter;
import com.italankin.lnch.feature.settings.hidden_items.HiddenItemsPresenter;
import com.italankin.lnch.feature.settings.lookfeel.LookAndFeelViewModel;
import com.italankin.lnch.feature.settings.preferencesearch.PreferenceSearchViewModel;
import com.italankin.lnch.model.backup.BackupReader;
import com.italankin.lnch.model.backup.BackupWriter;
import dagger.Component;

@ViewModelScope
@Component(dependencies = MainComponent.class)
public interface ViewModelComponent {

    AppsPresenter apps();

    FolderViewModel folder();

    EditFolderViewModel editFolder();

    AppsSettingsViewModel appsSettings();

    HiddenItemsPresenter hiddenItems();

    FontsPresenter fonts();

    AppDetailsViewModel appDetails();

    AppAliasesViewModel appAliases();

    LookAndFeelViewModel lookAndFeel();

    BackupPresenter backup();

    PreferenceSearchViewModel preferenceSearch();

    ComponentSelectorViewModel componentSelector();

    interface Dependencies {

        Context getContext();

        BackupReader getBackupReader();

        BackupWriter getBackupWriter();
    }
}
