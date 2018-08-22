package com.italankin.lnch.feature.home;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.base.state.OneExecutionTagStrategy;
import com.italankin.lnch.feature.base.state.TagStrategy;
import com.italankin.lnch.feature.home.model.ItemViewModel;
import com.italankin.lnch.feature.home.model.UserPrefs;

import java.util.List;

interface HomeView extends MvpView {

    String CONTENT = "content";
    String CUSTOMIZE = "customize";

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void showProgress();

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onAppsLoaded(List<ItemViewModel> appList, UserPrefs userPrefs);

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onAppsLoadError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(value = TagStrategy.class, tag = CUSTOMIZE)
    void onStartCustomize();

    @StateStrategyType(value = OneExecutionTagStrategy.class, tag = CUSTOMIZE)
    void onStopCustomize();

    @StateStrategyType(value = OneExecutionTagStrategy.class, tag = CUSTOMIZE)
    void onChangesDiscarded();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsSwap(int from, int to);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemInserted(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsInserted(int startIndex, int count);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsRemoved(int startIndex, int count);

}
