package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;

public class DeepShortcutDescriptorUiAdapter
        extends HomeAdapterDelegate<DeepShortcutDescriptorUiAdapter.ViewHolder, DeepShortcutDescriptorUi> {

    private static final float DISABLED_ALPHA = 0.5f;

    private final Listener listener;

    public DeepShortcutDescriptorUiAdapter(Listener listener) {
        this(listener, Params.DEFAULT);
    }

    public DeepShortcutDescriptorUiAdapter(Listener listener, Params params) {
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
                listener.onDeepShortcutClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDeepShortcutLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    protected boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof DeepShortcutDescriptorUi &&
                (ignoreVisibility || !((DeepShortcutDescriptorUi) item).isIgnored());
    }

    public interface Listener {
        void onDeepShortcutClick(int position, DeepShortcutDescriptorUi item);

        void onDeepShortcutLongClick(int position, DeepShortcutDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<DeepShortcutDescriptorUi> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        protected void bind(DeepShortcutDescriptorUi item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            label.setAlpha(item.enabled ? 1 : DISABLED_ALPHA);
        }

        @Override
        protected View getRoot() {
            return label;
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }
    }
}
