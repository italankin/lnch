package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;

import androidx.annotation.Nullable;

public interface DeepShortcutClickDelegate {

    void onDeepShortcutClick(DeepShortcutDescriptorUi item, @Nullable View itemView);

    void onDeepShortcutLongClick(DeepShortcutDescriptorUi item, @Nullable View itemView);
}
