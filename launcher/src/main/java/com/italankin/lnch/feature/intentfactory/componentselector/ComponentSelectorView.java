package com.italankin.lnch.feature.intentfactory.componentselector;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;

import java.util.List;

interface ComponentSelectorView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onComponentsLoaded(List<ComponentNameUi> componentNames);
}
