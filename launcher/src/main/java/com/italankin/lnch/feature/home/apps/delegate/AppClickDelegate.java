package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import androidx.annotation.Nullable;

public interface AppClickDelegate {

    void onAppClick(AppDescriptorUi item, @Nullable View itemView);

    void onAppLongClick(AppDescriptorUi item, @Nullable View itemView);
}

