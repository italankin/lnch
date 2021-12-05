package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;
import android.widget.PopupWindow;

import com.italankin.lnch.util.widget.ActionPopupWindow;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PopupDelegateImpl implements PopupDelegate {

    private final RecyclerView list;
    private PopupWindow popupWindow;

    public PopupDelegateImpl(RecyclerView list) {
        this.list = list;
    }

    @Override
    public void showPopupWindow(ActionPopupWindow popup, @Nullable View anchor) {
        if (anchor == null) {
            return;
        }
        dismissPopup();
        list.suppressLayout(true);
        popup.setOnDismissListener(() -> list.suppressLayout(false));
        popup.showAtAnchor(anchor, list);
        popupWindow = popup;
    }

    @Override
    public boolean dismissPopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
            return true;
        }
        return false;
    }
}
