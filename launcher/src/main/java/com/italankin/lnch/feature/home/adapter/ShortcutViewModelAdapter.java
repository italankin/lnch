package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.viewmodel.impl.PinnedShortcutViewModel;

public class ShortcutViewModelAdapter extends BaseHomeAdapterDelegate<ShortcutViewModelHolder, PinnedShortcutViewModel> {

    private final UserPrefs userPrefs;
    private final Listener listener;

    public ShortcutViewModelAdapter(UserPrefs userPrefs, Listener listener) {
        this.userPrefs = userPrefs;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected ShortcutViewModelHolder createViewHolder(View itemView) {
        ShortcutViewModelHolder holder = new ShortcutViewModelHolder(itemView);
        if (listener != null) {
            itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onShortcutClick(pos, getItem(pos));
                }
            });
            itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onShortcutLongClick(pos, getItem(pos));
                }
                return true;
            });
        }
        applyUserPrefs(holder.label, userPrefs);
        return holder;
    }

    @Override
    public void onBind(ShortcutViewModelHolder holder, int position, PinnedShortcutViewModel item) {
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
        void onShortcutClick(int position, PinnedShortcutViewModel item);

        void onShortcutLongClick(int position, PinnedShortcutViewModel item);
    }
}

class ShortcutViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    ShortcutViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(PinnedShortcutViewModel item) {
        label.setText(item.getVisibleLabel());
        label.setTextColor(item.getVisibleColor());
    }
}
