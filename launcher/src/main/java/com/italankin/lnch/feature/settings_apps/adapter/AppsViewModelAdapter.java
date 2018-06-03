package com.italankin.lnch.feature.settings_apps.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class AppsViewModelAdapter extends BaseAdapterDelegate<AppViewModelHolder, AppViewModel> {
    private final Listener listener;

    public AppsViewModelAdapter(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app_visibility;
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
        return holder;
    }

    @Override
    public void onBind(AppViewModelHolder holder, int position, AppViewModel item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppViewModel;
    }

    public interface Listener {
        void onItemClick(int position, AppViewModel item);
    }
}

class AppViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;
    final ImageView icon;

    AppViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
        icon = itemView.findViewById(R.id.icon);
    }

    void bind(AppViewModel item) {
        label.setCompoundDrawablesWithIntrinsicBounds(0,0,
                item.hidden ? R.drawable.ic_visibility_off : R.drawable.ic_visibility_on,0);
        label.setText(item.label);
        icon.setImageDrawable(item.icon);
        itemView.invalidate();
    }
}