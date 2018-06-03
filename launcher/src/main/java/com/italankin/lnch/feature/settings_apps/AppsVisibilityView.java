package com.italankin.lnch.feature.settings_apps;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;

import java.util.List;

interface AppsVisibilityView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onAppsLoaded(List<AppViewModel> apps);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

}
