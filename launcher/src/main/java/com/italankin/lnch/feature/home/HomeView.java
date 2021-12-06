package com.italankin.lnch.feature.home;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

public interface HomeView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void onAppsColorOverlayChanged(Integer color);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void onStatusBarColorChanged(Integer color);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void onHomePagerIndicatorVisibilityChanged(boolean visible);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onWidgetPreferencesUpdated();
}
