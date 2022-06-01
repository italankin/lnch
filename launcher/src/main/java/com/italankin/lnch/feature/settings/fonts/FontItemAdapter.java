package com.italankin.lnch.feature.settings.fonts;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class FontItemAdapter extends BaseAdapterDelegate<FontItemAdapter.ViewHolder, FontItem> {

    private final Listener listener;

    public FontItemAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_font;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFontSelect(pos, getItem(pos));
            }
        });
        holder.delete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFontDelete(pos, getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, FontItem item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, FontItem item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof FontItem;
    }

    public interface Listener {
        void onFontSelect(int position, FontItem item);

        void onFontDelete(int position, FontItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView preview;
        final View delete;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            preview = itemView.findViewById(R.id.preview);
            delete = itemView.findViewById(R.id.delete);
        }

        void bind(FontItem item) {
            name.setText(item.name);
            preview.setTypeface(item.typeface);
            preview.setText(item.previewText);
            delete.setVisibility(item.isDefault ? View.GONE : View.VISIBLE);
        }
    }
}
