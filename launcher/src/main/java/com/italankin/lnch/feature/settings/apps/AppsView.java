package com.italankin.lnch.feature.settings.apps;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.settings.apps.model.AppWithIconViewModel;

import java.util.List;

interface AppsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showLoading();

    @StateStrategyType(SingleStateStrategy.class)
    void onAppsLoaded(List<AppWithIconViewModel> apps);

    @StateStrategyType(SingleStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

}
