package com.italankin.lnch.feature.settings.apps.details;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

public interface AppDetailsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onModelLoaded(AppDetailsModel appDetailsModel);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable e);
}
