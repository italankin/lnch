package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;

public class PinnedShortcutDescriptorUiAdapter
        extends HomeAdapterDelegate<PinnedShortcutDescriptorUiAdapter.ViewHolder, PinnedShortcutDescriptorUi> {

    private final Listener listener;

    public PinnedShortcutDescriptorUiAdapter(Listener listener) {
        this(listener, Params.DEFAULT);
    }

    public PinnedShortcutDescriptorUiAdapter(Listener listener, Params params) {
        super(params);
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
                listener.onPinnedShortcutClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onPinnedShortcutLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof PinnedShortcutDescriptorUi &&
                (ignoreVisibility || !((PinnedShortcutDescriptorUi) item).isIgnored());
    }

    public interface Listener {
        void onPinnedShortcutClick(int position, PinnedShortcutDescriptorUi item);

        void onPinnedShortcutLongClick(int position, PinnedShortcutDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<PinnedShortcutDescriptorUi> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        protected void bind(PinnedShortcutDescriptorUi item) {
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
