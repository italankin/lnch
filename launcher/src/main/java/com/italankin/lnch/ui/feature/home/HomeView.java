package com.italankin.lnch.ui.feature.home;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.ui.base.state.OneExecutionTagStrategy;
import com.italankin.lnch.ui.base.state.TagStrategy;

import java.util.List;

interface HomeView extends MvpView {

    String CONTENT = "content";
    String EDIT_MODE = "edit_mode";

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void showProgress();

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onAppsLoaded(List<AppItem> appList, SearchRepository searchRepository, String layout);

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
