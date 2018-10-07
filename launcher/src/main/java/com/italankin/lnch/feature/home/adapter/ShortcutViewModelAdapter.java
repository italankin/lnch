package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.descriptor.model.ShortcutViewModel;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class ShortcutViewModelAdapter extends BaseAdapterDelegate<ShortcutViewModelHolder, ShortcutViewModel> {
    private final Listener listener;

    public ShortcutViewModelAdapter(Listener listener) {
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
        return holder;
    }

    @Override
    public void onBind(ShortcutViewModelHolder holder, int position, ShortcutViewModel item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, ShortcutViewModel item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof ShortcutViewModel;
    }

    public interface Listener {
        void onShortcutClick(int position, ShortcutViewModel item);

        void onShortcutLongClick(int position, ShortcutViewModel item);
    }
}

class ShortcutViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    ShortcutViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(ShortcutViewModel item) {
        label.setText(item.getVisibleLabel());
        label.setTextColor(item.getVisibleColor());
    }
}
