package com.italankin.lnch.feature.home;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.base.state.OneExecutionTagStrategy;
import com.italankin.lnch.feature.base.state.TagStrategy;
import com.italankin.lnch.feature.home.model.AppViewModel;

import java.util.List;

interface HomeView extends MvpView {

    String CONTENT = "content";
    String EDIT_MODE = "edit_mode";

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void showProgress();

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onAppsLoaded(List<AppViewModel> appList, String layout);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(value = TagStrategy.class, tag = EDIT_MODE)
    void onStartEditMode();

    @StateStrategyType(value = OneExecutionTagStrategy.class, tag = EDIT_MODE)
    void onStopEditMode();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsSwap(int from, int to);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

}
