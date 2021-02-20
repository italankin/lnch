package com.italankin.lnch.feature.settings.apps.details.aliases;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

public interface AppAliasesView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onAliasesChanged(List<String> aliases, boolean canAddMore);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void notifyAliasRemoved(int size, boolean canAddMore);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void notifyAliasAdded(int size, boolean canAddMore);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onInvalidAlias();
}
