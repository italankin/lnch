package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.viewmodel.impl.DeepShortcutViewModel;

public class DeepShortcutViewModelAdapter
        extends HomeAdapterDelegate<DeepShortcutViewModelAdapter.ViewHolder, DeepShortcutViewModel> {

    private static final float DISABLED_ALPHA = 0.5f;

    private final Listener listener;

    public DeepShortcutViewModelAdapter(Listener listener) {
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
                listener.onDeepShortcutClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDeepShortcutLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof DeepShortcutViewModel && ((DeepShortcutViewModel) item).isVisible();
    }

    public interface Listener {
        void onDeepShortcutClick(int position, DeepShortcutViewModel item);

        void onDeepShortcutLongClick(int position, DeepShortcutViewModel item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<DeepShortcutViewModel> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        void bind(DeepShortcutViewModel item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            label.setAlpha(item.enabled ? 1 : DISABLED_ALPHA);
        }

        @Nullable
        @Override
        TextView getLabel() {
            return label;
        }
    }
}
