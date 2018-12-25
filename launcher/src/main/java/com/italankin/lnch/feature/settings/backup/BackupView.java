package com.italankin.lnch.feature.settings.backup;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

interface BackupView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRestoreSuccess();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRestoreError(Throwable error);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onBackupSuccess(String path);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onBackupError(Throwable error);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onResetSuccess();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onResetError(Throwable error);
}
