package com.italankin.lnch.feature.settings.hidden_items;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

interface HiddenItemsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showLoading();

    @StateStrategyType(SingleStateStrategy.class)
    void onItemsUpdated(List<HiddenItem> items);

    @StateStrategyType(SingleStateStrategy.class)
    void showError(Throwable e);
}
