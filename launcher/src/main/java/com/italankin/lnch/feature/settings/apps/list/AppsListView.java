package com.italankin.lnch.feature.settings.apps.list;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import java.util.List;

interface AppsListView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showLoading();

    @StateStrategyType(SingleStateStrategy.class)
    void onAppsLoaded(List<AppDescriptorUi> apps);

    @StateStrategyType(SingleStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

}
