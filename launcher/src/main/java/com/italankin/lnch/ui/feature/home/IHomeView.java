package com.italankin.lnch.ui.feature.home;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.PackageModel;

import java.util.List;

interface IHomeView extends MvpView {

    @StateStrategyType(value = SingleStateStrategy.class)
    void showProgress();

    @StateStrategyType(value = SingleStateStrategy.class)
    void hideProgress();

    @StateStrategyType(value = SingleStateStrategy.class)
    void onAppsLoaded(List<PackageModel> appList);

}
