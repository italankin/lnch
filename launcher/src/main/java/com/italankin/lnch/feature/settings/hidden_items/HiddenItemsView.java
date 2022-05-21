package com.italankin.lnch.feature.settings.hidden_items;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;

import java.util.List;

interface HiddenItemsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showLoading();

    @StateStrategyType(SingleStateStrategy.class)
    void onItemsUpdated(List<IgnorableDescriptorUi> items);

    @StateStrategyType(SingleStateStrategy.class)
    void showError(Throwable e);
}
