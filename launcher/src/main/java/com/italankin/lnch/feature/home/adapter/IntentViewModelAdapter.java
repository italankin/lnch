package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.viewmodel.impl.IntentViewModel;

public class IntentViewModelAdapter
        extends HomeAdapterDelegate<IntentViewModelAdapter.ViewHolder, IntentViewModel> {

    private final Listener listener;

    public IntentViewModelAdapter(Listener listener) {
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
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onIntentClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onIntentLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof IntentViewModel && ((IntentViewModel) item).isVisible();
    }

    public interface Listener {
        void onIntentClick(int position, IntentViewModel item);

        void onIntentLongClick(int position, IntentViewModel item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<IntentViewModel> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        void bind(IntentViewModel item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
        }

        @Nullable
        @Override
        TextView getLabel() {
            return label;
        }
    }
}
