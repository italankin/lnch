package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;

interface FolderView extends BaseFolderView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutPinned(Shortcut shortcut);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutAlreadyPinnedError(Shortcut shortcut);
}
