package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.util.widget.ActionPopupWindow;

import androidx.annotation.Nullable;

public interface PopupDelegate {

    void showPopupWindow(ActionPopupWindow popup, @Nullable View anchor);

    boolean dismissPopup();
}
