package com.italankin.lnch.feature.widgets.adapter;

import com.italankin.lnch.feature.widgets.model.AppWidget;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

class WidgetDiffUtilCallback extends DiffUtil.ItemCallback<AppWidget> {

    @Override
    public boolean areItemsTheSame(@NonNull AppWidget oldItem, @NonNull AppWidget newItem) {
        return oldItem.appWidgetId == newItem.appWidgetId;
    }

    @Override
    public boolean areContentsTheSame(@NonNull AppWidget oldItem, @NonNull AppWidget newItem) {
        return oldItem.equals(newItem);
    }
}
