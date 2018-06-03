package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class AppViewModelAdapter extends BaseAdapterDelegate<AppViewModelHolder, AppViewModel> {
    private final Listener listener;

    public AppViewModelAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected AppViewModelHolder createViewHolder(View itemView) {
        AppViewModelHolder holder = new AppViewModelHolder(itemView);
        itemView.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pos, getItem(pos));
                }
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(pos, getItem(pos));
                    return true;
                }
            }
            return false;
        });
        return holder;
    }

    @Override
    public long getItemId(int position, AppViewModel item) {
        return item.hashCode();
    }

    @Override
    public void onBind(AppViewModelHolder holder, int position, AppViewModel item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppViewModel && !((AppViewModel) item).hidden;
    }

    public interface Listener {
        void onItemClick(int position, AppViewModel item);

        void onItemLongClick(int position, AppViewModel item);
    }
}

class AppViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    AppViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(AppViewModel item) {
        label.setText(item.getLabel());
        label.setTextColor(item.getColor());
    }
}