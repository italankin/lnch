package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.List;

interface FolderView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onShowFolder(GroupDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(Throwable error);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutPinned(Shortcut shortcut);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutAlreadyPinnedError(Shortcut shortcut);
}
