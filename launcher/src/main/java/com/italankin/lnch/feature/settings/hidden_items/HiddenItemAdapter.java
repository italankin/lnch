package com.italankin.lnch.feature.settings.hidden_items;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;
import com.squareup.picasso.Picasso;

public class HiddenItemAdapter extends BaseAdapterDelegate<HiddenItemAdapter.ViewHolder, HiddenItem> {

    private final Picasso picasso;
    private final Listener listener;

    public HiddenItemAdapter(Picasso picasso, Listener listener) {
        this.picasso = picasso;
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
                picasso.load(item.uri)
                        .fit()
                        .into(icon);
            } else {
                picasso.cancelRequest(icon);
                icon.setImageResource(R.drawable.ic_settings_apps);
            }
        }
    }
}
