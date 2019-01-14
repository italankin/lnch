package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.viewmodel.impl.PinnedShortcutViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PinnedShortcutViewModelAdapter
        extends HomeAdapterDelegate<PinnedShortcutViewModelAdapter.ViewHolder, PinnedShortcutViewModel> {

    private final Listener listener;

    public PinnedShortcutViewModelAdapter(Listener listener) {
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
                listener.onPinnedShortcutClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onPinnedShortcutLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof PinnedShortcutViewModel && ((PinnedShortcutViewModel) item).isVisible();
    }

    public interface Listener {
        void onPinnedShortcutClick(int position, PinnedShortcutViewModel item);

        void onPinnedShortcutLongClick(int position, PinnedShortcutViewModel item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<PinnedShortcutViewModel> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        void bind(PinnedShortcutViewModel item) {
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
