package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

import androidx.annotation.Nullable;

public class IntentClickDelegateImpl implements IntentClickDelegate {

    private final SearchIntentStarterDelegate searchIntentStarterDelegate;
    private final ItemPopupDelegate itemPopupDelegate;

    public IntentClickDelegateImpl(SearchIntentStarterDelegate searchIntentStarterDelegate,
            ItemPopupDelegate itemPopupDelegate) {
        this.searchIntentStarterDelegate = searchIntentStarterDelegate;
        this.itemPopupDelegate = itemPopupDelegate;
    }

    @Override
    public void onIntentClick(IntentDescriptorUi item) {
        searchIntentStarterDelegate.handleSearchIntent(item.intent, item.getDescriptor());
    }

    @Override
    public void onIntentLongClick(IntentDescriptorUi item, @Nullable View itemView) {
        itemPopupDelegate.showItemPopup(item, itemView);
    }
}
