package com.italankin.lnch.feature.settings.hidden_items;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import me.italankin.adapterdelegates.BaseAdapterDelegate;
import com.italankin.lnch.util.imageloader.ImageLoader;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HiddenItemAdapter extends BaseAdapterDelegate<HiddenItemAdapter.ViewHolder, HiddenItem> {

    private final ImageLoader imageLoader;
    private final Listener listener;

    public HiddenItemAdapter(ImageLoader imageLoader, Listener listener) {
        this.imageLoader = imageLoader;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_hidden;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.visibility.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onVisibilityClick(getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, HiddenItem item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, HiddenItem item) {
        return item.descriptor.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof HiddenItem;
    }

    public interface Listener {
        void onVisibilityClick(HiddenItem item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView originalLabel;
        final TextView label;
        final ImageView visibility;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            originalLabel = itemView.findViewById(R.id.original_label);
            label = itemView.findViewById(R.id.label);
            visibility = itemView.findViewById(R.id.visibility);
        }

        void bind(HiddenItem item) {
            originalLabel.setText(item.originalLabel);
            label.setText(item.visibleLabel);
            if (item.uri != null) {
                imageLoader.load(item.uri)
                        .into(icon);
            } else {
                imageLoader.cancel(icon);
                icon.setImageResource(R.drawable.ic_settings_apps);
            }
        }
    }
}
