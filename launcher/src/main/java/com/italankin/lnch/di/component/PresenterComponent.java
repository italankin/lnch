package com.italankin.lnch.di.component;

import com.italankin.lnch.di.scope.AppScope;
import com.italankin.lnch.feature.home.HomePresenter;
import com.italankin.lnch.feature.settings_apps.AppsVisibilityPresenter;

import dagger.Component;

@AppScope
@Component(dependencies = {MainComponent.class})
public interface PresenterComponent {

    HomePresenter home();

    AppsVisibilityPresenter appsVisibility();

}
