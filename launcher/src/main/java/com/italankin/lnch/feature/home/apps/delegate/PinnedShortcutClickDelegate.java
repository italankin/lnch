package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;

import androidx.annotation.Nullable;

public interface PinnedShortcutClickDelegate {

    void onPinnedShortcutClick(PinnedShortcutDescriptorUi item);

    void onPinnedShortcutLongClick(PinnedShortcutDescriptorUi item, @Nullable View itemView);
}
