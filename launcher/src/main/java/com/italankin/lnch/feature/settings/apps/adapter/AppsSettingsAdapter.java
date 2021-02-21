package com.italankin.lnch.feature.settings.apps.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;
import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppsSettingsAdapter
        extends BaseAdapterDelegate<AppsSettingsAdapter.ViewHolder, AppDescriptorUi> {

    private final Picasso picasso;
    private final Listener listener;

    public AppsSettingsAdapter(Picasso picasso, Listener listener) {
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
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppClick(pos, getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public long getItemId(int position, AppDescriptorUi item) {
        return item.getDescriptor().getId().hashCode();
    }

    @Override
    public void onBind(ViewHolder holder, int position, AppDescriptorUi item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppDescriptorUi;
    }

    public interface Listener {
        void onAppClick(int position, AppDescriptorUi item);

        void onVisibilityClick(int position, AppDescriptorUi item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private static final float ALPHA_ITEM_VISIBLE = 1f;
        private static final float ALPHA_ITEM_IGNORED = 0.3f;

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

        void bind(AppDescriptorUi item) {
            visibility.setImageResource(item.isIgnored() ? R.drawable.ic_visibility_off :
                    R.drawable.ic_visibility_on);
            label.setText(item.getVisibleLabel());
            packageName.setText(item.packageName);
            picasso.load(PackageIconHandler.uriFrom(item.packageName))
                    .fit()
                    .into(icon);
            float alpha = item.isIgnored() ? ALPHA_ITEM_IGNORED : ALPHA_ITEM_VISIBLE;
            label.setAlpha(alpha);
            icon.setAlpha(alpha);
        }
    }
}
