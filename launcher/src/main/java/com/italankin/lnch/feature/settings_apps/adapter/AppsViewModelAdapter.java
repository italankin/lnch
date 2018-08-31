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
import com.squareup.picasso.Picasso;

public class AppsViewModelAdapter extends BaseAdapterDelegate<AppsViewModelAdapter.AppViewModelHolder, AppViewModel> {
    private final Picasso picasso;
    private final Listener listener;

    public AppsViewModelAdapter(Picasso picasso, @Nullable Listener listener) {
        this.picasso = picasso;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_app;
    }

    @NonNull
    @Override
    protected AppViewModelHolder createViewHolder(View itemView) {
        AppViewModelHolder holder = new AppViewModelHolder(itemView);
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pos, getItem(pos));
                }
            });
            holder.visibility.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onVisibilityClick(pos, getItem(pos));
                }
            });
        }
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

        void onVisibilityClick(int position, AppViewModel item);
    }

    class AppViewModelHolder extends RecyclerView.ViewHolder {
        private static final float ALPHA_ITEM_VISIBLE = 1f;
        private static final float ALPHA_ITEM_HIDDEN = 0.3f;

        final ImageView icon;
        final TextView label;
        final TextView packageName;
        final ImageView visibility;

        AppViewModelHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            packageName = itemView.findViewById(R.id.package_name);
            icon = itemView.findViewById(R.id.icon);
            visibility = itemView.findViewById(R.id.visibility);
        }

        void bind(AppViewModel item) {
            visibility.setImageResource(item.hidden ? R.drawable.ic_visibility_off :
                    R.drawable.ic_visibility_on);
            label.setText(item.label);
            packageName.setText(item.packageName);
            picasso.load(item.icon)
                    .fit()
                    .into(icon);
            float alpha = item.hidden ? ALPHA_ITEM_HIDDEN : ALPHA_ITEM_VISIBLE;
            label.setAlpha(alpha);
            icon.setAlpha(alpha);
        }
    }
}