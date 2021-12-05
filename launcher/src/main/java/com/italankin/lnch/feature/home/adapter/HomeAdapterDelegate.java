package com.italankin.lnch.feature.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

abstract class HomeAdapterDelegate<VH extends HomeAdapterDelegate.ViewHolder<T>, T extends DescriptorUi>
        extends BaseAdapterDelegate<VH, T> {

    private UserPrefs.ItemPrefs itemPrefs;

    @Override
    public final void onBind(VH holder, int position, T item) {
        updateHolderView(holder);
        holder.bind(item);
    }

    @Override
    public final void onBind(VH holder, int position, T item, List<Object> payloads) {
        updateHolderView(holder);
        holder.bind(item, payloads);
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

    private void updateHolderView(VH holder) {
        try {
            TextView label = holder.getLabel();
            if (label == null || itemPrefs == null || itemPrefs.equals(holder.itemPrefs)) {
                return;
            }
            update(holder, label, itemPrefs);
        } finally {
            holder.itemPrefs = itemPrefs;
        }
    }

    protected void update(VH holder, TextView label, UserPrefs.ItemPrefs itemPrefs) {
        ViewUtils.setPaddingDp(label, itemPrefs.itemPadding);
        label.setTextSize(itemPrefs.itemTextSize);
        int shadowColor = itemPrefs.itemShadowColor != null
                ? itemPrefs.itemShadowColor
                : ResUtils.resolveColor(label.getContext(), R.attr.colorItemShadowDefault);
        label.setShadowLayer(itemPrefs.itemShadowRadius, label.getShadowDx(),
                label.getShadowDy(), shadowColor);
        label.setTypeface(itemPrefs.itemFont.typeface());
    }

    abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        UserPrefs.ItemPrefs itemPrefs;

        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bind(T item);

        protected void bind(T item, List<Object> payloads) {
            bind(item);
        }

        @Nullable
        abstract TextView getLabel();
    }
}
