package com.italankin.lnch.di.component;

import com.italankin.lnch.di.scope.AppScope;
import com.italankin.lnch.feature.home.apps.AppsPresenter;
import com.italankin.lnch.feature.settings.apps.list.AppsListPresenter;
import com.italankin.lnch.feature.settings.backup.BackupPresenter;
import com.italankin.lnch.feature.settings.lookfeel.LookAndFeelPresenter;
import com.italankin.lnch.feature.widgets.WidgetsPresenter;

import dagger.Component;

@AppScope
@Component(dependencies = MainComponent.class)
public interface PresenterComponent {

    AppsPresenter apps();

    AppsListPresenter appsList();

    LookAndFeelPresenter lookAndFeel();

    BackupPresenter backup();

    WidgetsPresenter widgets();
}
