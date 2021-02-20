package com.italankin.lnch.feature.settings.apps.details;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

public interface AppDetailsView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onDescriptorLoaded(AppDescriptor descriptor);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable e);
}
