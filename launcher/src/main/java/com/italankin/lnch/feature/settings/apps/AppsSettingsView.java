package com.italankin.lnch.feature.settings.apps;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import java.util.List;

interface AppsSettingsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showLoading();

    @StateStrategyType(SingleStateStrategy.class)
    void onAppsUpdated(List<AppDescriptorUi> apps);

    @StateStrategyType(SingleStateStrategy.class)
    void showError(Throwable e);
}
