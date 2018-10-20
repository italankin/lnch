package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.viewmodel.impl.PinnedShortcutViewModel;

public class PinnedShortcutViewModelAdapter extends
        BaseHomeAdapterDelegate<PinnedShortcutViewModelAdapter.ViewHolder, PinnedShortcutViewModel> {

    private final UserPrefs userPrefs;
    private final Listener listener;

    public PinnedShortcutViewModelAdapter(UserPrefs userPrefs, Listener listener) {
        this.userPrefs = userPrefs;
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
        if (listener != null) {
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
        }
        applyUserPrefs(holder.label, userPrefs);
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, PinnedShortcutViewModel item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, PinnedShortcutViewModel item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof PinnedShortcutViewModel && ((PinnedShortcutViewModel) item).isVisible();
    }

    public interface Listener {
        void onPinnedShortcutClick(int position, PinnedShortcutViewModel item);

        void onPinnedShortcutLongClick(int position, PinnedShortcutViewModel item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        void bind(PinnedShortcutViewModel item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
        }
    }
}
