package com.italankin.lnch.feature.home.apps;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.italankin.lnch.feature.base.state.OneExecutionTagStrategy;
import com.italankin.lnch.feature.base.state.TagStrategy;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

import java.util.List;

interface AppsView extends MvpView {

    String CONTENT = "content";
    String CUSTOMIZE = "customize";

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void showProgress();

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onReceiveUpdate(Update update);

    @StateStrategyType(value = SingleStateStrategy.class, tag = CONTENT)
    void onReceiveUpdateError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showError(Throwable e);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectFolderDialog(int position, InFolderDescriptorUi item, List<FolderDescriptorUi> folders);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showItemRenameDialog(int position, CustomLabelDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSetItemColorDialog(int position, CustomColorDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showIntentEditor(IntentDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showFolder(int position, FolderDescriptor descriptor);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFolderUpdated(FolderDescriptorUi item, boolean added);

    @StateStrategyType(value = TagStrategy.class, tag = CUSTOMIZE)
    void onStartCustomize();

    @StateStrategyType(value = OneExecutionTagStrategy.class, tag = CUSTOMIZE)
    void onStopCustomize();

    @StateStrategyType(value = OneExecutionStateStrategy.class)
    void onConfirmDiscardChanges();

    @StateStrategyType(value = OneExecutionTagStrategy.class, tag = CUSTOMIZE)
    void onChangesDiscarded();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteDialog(RemovableDescriptorUi item);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutPinned(Shortcut shortcut);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShortcutAlreadyPinnedError(Shortcut shortcut);
}
