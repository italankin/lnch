package com.italankin.lnch.feature.settings.apps.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.apps.model.AppWithIconViewModel;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;
import com.squareup.picasso.Picasso;

public class AppsViewModelAdapter extends BaseAdapterDelegate<AppsViewModelAdapter.ViewHolder, AppWithIconViewModel> {
    private final Picasso picasso;
    private final Listener listener;

    public AppsViewModelAdapter(Picasso picasso, Listener listener) {
        this.picasso = picasso;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_app;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.visibility.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onVisibilityClick(pos, getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, AppWithIconViewModel item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppWithIconViewModel;
    }

    public interface Listener {
        void onVisibilityClick(int position, AppViewModel item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private static final float ALPHA_ITEM_VISIBLE = 1f;
        private static final float ALPHA_ITEM_HIDDEN = 0.3f;

        final ImageView icon;
        final TextView label;
        final TextView packageName;
        final ImageView visibility;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            packageName = itemView.findViewById(R.id.package_name);
            icon = itemView.findViewById(R.id.icon);
            visibility = itemView.findViewById(R.id.visibility);
        }

        void bind(AppWithIconViewModel item) {
            visibility.setImageResource(item.isHidden() ? R.drawable.ic_visibility_off :
                    R.drawable.ic_visibility_on);
            label.setText(item.getVisibleLabel());
            packageName.setText(item.packageName);
            picasso.load(item.icon)
                    .fit()
                    .into(icon);
            float alpha = item.isHidden() ? ALPHA_ITEM_HIDDEN : ALPHA_ITEM_VISIBLE;
            label.setAlpha(alpha);
            icon.setAlpha(alpha);
        }
    }
}