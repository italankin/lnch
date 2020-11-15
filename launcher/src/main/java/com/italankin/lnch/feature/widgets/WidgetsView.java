package com.italankin.lnch.feature.widgets;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

interface WidgetsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onBindWidgets(List<Integer> appWidgetIds);
}
