package com.italankin.lnch.feature.settings.backup;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

interface BackupView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void showProgress();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRestoreSuccess();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRestoreError(Throwable error);
}
