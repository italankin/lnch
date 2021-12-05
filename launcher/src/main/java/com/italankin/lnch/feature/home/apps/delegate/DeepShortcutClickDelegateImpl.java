package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;

import androidx.annotation.Nullable;

public class DeepShortcutClickDelegateImpl implements DeepShortcutClickDelegate {

    private final ShortcutStarterDelegate shortcutStarterDelegate;
    private final ItemPopupDelegate itemPopupDelegate;
    private final ShortcutsRepository shortcutsRepository;

    public DeepShortcutClickDelegateImpl(ShortcutStarterDelegate shortcutStarterDelegate,
            ItemPopupDelegate itemPopupDelegate, ShortcutsRepository shortcutsRepository) {
        this.shortcutStarterDelegate = shortcutStarterDelegate;
        this.itemPopupDelegate = itemPopupDelegate;
        this.shortcutsRepository = shortcutsRepository;
    }

    @Override
    public void onDeepShortcutClick(DeepShortcutDescriptorUi item, @Nullable View itemView) {
        Shortcut shortcut = shortcutsRepository.getShortcut(item.packageName, item.id);
        shortcutStarterDelegate.startShortcut(shortcut, itemView);
    }

    @Override
    public void onDeepShortcutLongClick(DeepShortcutDescriptorUi item, @Nullable View itemView) {
        itemPopupDelegate.showItemPopup(item, itemView);
    }
}
