package com.italankin.lnch.feature.intentfactory.componentselector;

import com.arellomobile.mvp.MvpView;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;

import java.util.List;

interface ComponentSelectorView extends MvpView {

    void onComponentsLoaded(List<ComponentNameUi> componentNames);
}
