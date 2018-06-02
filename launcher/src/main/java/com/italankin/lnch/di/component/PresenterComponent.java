package com.italankin.lnch.di.component;

import com.italankin.lnch.di.scope.AppScope;
import com.italankin.lnch.ui.feature.home.HomePresenter;
import com.italankin.lnch.ui.feature.settings.visibility.AppsVisibilityPresenter;

import dagger.Component;

@AppScope
@Component(dependencies = {MainComponent.class})
public interface PresenterComponent {

    HomePresenter home();

    AppsVisibilityPresenter appsVisibility();

}
