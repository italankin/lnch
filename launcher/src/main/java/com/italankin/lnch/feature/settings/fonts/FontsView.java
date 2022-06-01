package com.italankin.lnch.feature.settings.fonts;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

interface FontsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onItemsUpdated(List<FontItem> items);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFontDeleted(boolean reset);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onAddFontExistsError(String name);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onAddFontEmptyNameError();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFontAdded();
}
