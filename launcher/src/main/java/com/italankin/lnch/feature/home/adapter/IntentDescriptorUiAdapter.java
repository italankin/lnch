package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

public class IntentDescriptorUiAdapter
        extends HomeAdapterDelegate<IntentDescriptorUiAdapter.ViewHolder, IntentDescriptorUi> {

    private final Listener listener;

    public IntentDescriptorUiAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onIntentClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onIntentLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof IntentDescriptorUi;
    }

    public interface Listener {
        void onIntentClick(int position, IntentDescriptorUi item);

        void onIntentLongClick(int position, IntentDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<IntentDescriptorUi> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        protected void bind(IntentDescriptorUi item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }
    }
}
