package com.italankin.lnch.feature.widgets.adapter;

import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.model.NoWidgetsItem;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoWidgetsAdapter extends BaseAdapterDelegate<NoWidgetsAdapter.NoWidgetsViewHolder, NoWidgetsItem> {

    private static final float SCREEN_HEIGHT_RATIO = .7f;

    @Override
    protected int getLayoutRes() {
        return R.layout.item_no_widgets;
    }

    @NonNull
    @Override
    protected NoWidgetsViewHolder createViewHolder(View itemView) {
        int height = (int) (itemView.getResources().getDisplayMetrics().heightPixels * SCREEN_HEIGHT_RATIO);
        itemView.setMinimumHeight(height);
        return new NoWidgetsViewHolder(itemView);
    }

    @Override
    public void onBind(NoWidgetsViewHolder holder, int position, NoWidgetsItem item) {
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof NoWidgetsItem;
    }

    static class NoWidgetsViewHolder extends RecyclerView.ViewHolder {
        NoWidgetsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
