package com.italankin.lnch.feature.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

abstract class BaseHomeAdapterDelegate<VH extends RecyclerView.ViewHolder, T> extends
        BaseAdapterDelegate<VH, T> {

    protected void applyUserPrefs(TextView label, UserPrefs userPrefs) {
        DisplayMetrics dm = label.getResources().getDisplayMetrics();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                userPrefs.itemPadding, dm);
        label.setPadding(padding, padding, padding, padding);
        label.setTextSize(userPrefs.itemTextSize);
        label.setShadowLayer(userPrefs.itemShadowRadius, label.getShadowDx(), label.getShadowDy(),
                label.getShadowColor());
        label.setTypeface(userPrefs.itemFont);
    }
}
