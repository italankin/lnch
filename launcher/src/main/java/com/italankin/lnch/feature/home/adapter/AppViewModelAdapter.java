package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.descriptor.model.AppViewModel;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class AppViewModelAdapter extends BaseAdapterDelegate<AppViewModelHolder, AppViewModel> {

    private final UserPrefs userPrefs;
    private final Listener listener;

    public AppViewModelAdapter(UserPrefs userPrefs, Listener listener) {
        this.userPrefs = userPrefs;
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
        if (listener != null) {
            itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onAppClick(pos, getItem(pos));
                }
            });
            itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onAppLongClick(pos, getItem(pos));
                    return true;
                }
                return false;
            });
        }
        applyUserPrefs(holder);
        return holder;
    }

    private void applyUserPrefs(AppViewModelHolder holder) {
        DisplayMetrics dm = holder.itemView.getResources().getDisplayMetrics();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                userPrefs.itemPadding, dm);
        holder.label.setPadding(padding, padding, padding, padding);
        holder.label.setTextSize(userPrefs.itemTextSize);
        holder.label.setShadowLayer(userPrefs.itemShadowRadius,
                holder.label.getShadowDx(), holder.label.getShadowDy(), holder.label.getShadowColor());
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
        return item instanceof AppViewModel && ((AppViewModel) item).isVisible();
    }

    public interface Listener {
        void onAppClick(int position, AppViewModel item);

        void onAppLongClick(int position, AppViewModel item);
    }
}

class AppViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    AppViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(AppViewModel item) {
        label.setText(item.getVisibleLabel());
        label.setTextColor(item.getVisibleColor());
    }
}