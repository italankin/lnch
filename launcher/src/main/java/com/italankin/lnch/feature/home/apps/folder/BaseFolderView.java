package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.List;

interface BaseFolderView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onShowFolder(String folderTitle, List<DescriptorUi> items, UserPrefs userPrefs, boolean animated);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFolderUpdated(List<DescriptorUi> items);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable error);
}
