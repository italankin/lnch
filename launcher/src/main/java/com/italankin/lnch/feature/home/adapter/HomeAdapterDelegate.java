package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

abstract class HomeAdapterDelegate<VH extends HomeAdapterDelegate.ViewHolder<T>, T extends DescriptorItem>
        extends BaseAdapterDelegate<VH, T> {

    private UserPrefs.ItemPrefs itemPrefs;

    @Override
    public void onBind(VH holder, int position, T item) {
        updateHolderView(holder);
        holder.bind(item);
    }

    @NonNull
    @Override
    public VH onCreate(LayoutInflater inflater, ViewGroup parent) {
        VH holder = super.onCreate(inflater, parent);
        updateHolderView(holder);
        return holder;
    }

    @Override
    public long getItemId(int position, T item) {
        return item.getDescriptor().getId().hashCode();
    }

    void setItemPrefs(UserPrefs.ItemPrefs itemPrefs) {
        this.itemPrefs = itemPrefs;
    }

    private void updateHolderView(ViewHolder<T> holder) {
        try {
            TextView label = holder.getLabel();
            if (label == null || itemPrefs == null || itemPrefs.equals(holder.itemPrefs)) {
                return;
            }
            int padding = ResUtils.px2dp(label.getContext(), itemPrefs.itemPadding);
            label.setPadding(padding, padding, padding, padding);
            label.setTextSize(itemPrefs.itemTextSize);
            label.setShadowLayer(itemPrefs.itemShadowRadius, label.getShadowDx(),
                    label.getShadowDy(), itemPrefs.itemShadowColor);
            label.setTypeface(itemPrefs.itemFont.typeface());
        } finally {
            holder.itemPrefs = itemPrefs;
        }
    }

    abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        UserPrefs.ItemPrefs itemPrefs;

        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bind(T item);

        @Nullable
        abstract TextView getLabel();
    }
}
