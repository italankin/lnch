package com.italankin.lnch.di.component;

import com.italankin.lnch.di.scope.AppScope;
import com.italankin.lnch.ui.feature.home.HomePresenter;

import dagger.Component;

@AppScope
@Component(dependencies = {MainComponent.class})
public interface PresenterComponent {

    HomePresenter home();

}
