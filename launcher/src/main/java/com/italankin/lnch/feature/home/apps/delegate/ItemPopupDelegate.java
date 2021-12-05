package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.ui.DescriptorUi;

import androidx.annotation.Nullable;

public interface ItemPopupDelegate {

    void showItemPopup(DescriptorUi item, @Nullable View anchor);
}
