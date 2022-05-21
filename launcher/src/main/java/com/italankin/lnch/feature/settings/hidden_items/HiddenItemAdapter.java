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

public class HiddenItemAdapter extends BaseAdapterDelegate<HiddenItemAdapter.ViewHolder, IgnorableDescriptorUi> {

    private final Listener listener;

    public HiddenItemAdapter(Listener listener) {
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
    public long getItemId(int position, IgnorableDescriptorUi item) {
        return item.getDescriptor().getId().hashCode();
    }

    @Override
    public void onBind(ViewHolder holder, int position, IgnorableDescriptorUi item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof IgnorableDescriptorUi;
    }

    public interface Listener {
        void onVisibilityClick(IgnorableDescriptorUi item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView label;
        final ImageView visibility;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            visibility = itemView.findViewById(R.id.visibility);
        }

        void bind(IgnorableDescriptorUi item) {
            label.setText(CustomLabelDescriptorUi.getVisibleLabel(item));
        }
    }
}
