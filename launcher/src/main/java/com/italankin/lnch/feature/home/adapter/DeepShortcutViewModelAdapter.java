package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.viewmodel.impl.DeepShortcutViewModel;

public class DeepShortcutViewModelAdapter extends BaseHomeAdapterDelegate<DeepShortcutViewModelHolder, DeepShortcutViewModel> {

    private final UserPrefs userPrefs;
    private final Listener listener;

    public DeepShortcutViewModelAdapter(UserPrefs userPrefs, Listener listener) {
        this.userPrefs = userPrefs;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected DeepShortcutViewModelHolder createViewHolder(View itemView) {
        DeepShortcutViewModelHolder holder = new DeepShortcutViewModelHolder(itemView);
        if (listener != null) {
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
        }
        applyUserPrefs(holder.label, userPrefs);
        return holder;
    }

    @Override
    public void onBind(DeepShortcutViewModelHolder holder, int position, DeepShortcutViewModel item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, DeepShortcutViewModel item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof DeepShortcutViewModel && ((DeepShortcutViewModel) item).isVisible();
    }

    public interface Listener {
        void onDeepShortcutClick(int position, DeepShortcutViewModel item);

        void onDeepShortcutLongClick(int position, DeepShortcutViewModel item);
    }
}

class DeepShortcutViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    DeepShortcutViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(DeepShortcutViewModel item) {
        label.setText(item.getVisibleLabel());
        label.setTextColor(item.getVisibleColor());
    }
}
