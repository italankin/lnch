package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

import androidx.annotation.Nullable;

public interface IntentClickDelegate {

    void onIntentClick(IntentDescriptorUi item);

    void onIntentLongClick(IntentDescriptorUi item, @Nullable View itemView);
}
