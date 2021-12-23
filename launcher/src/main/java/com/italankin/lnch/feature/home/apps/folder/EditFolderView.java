package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;

interface EditFolderView extends BaseFolderView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShowRenameDialog(int position, CustomLabelDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShowSetColorDialog(int position, CustomColorDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemChanged(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemRemoved(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsSwap(int from, int to);
}
