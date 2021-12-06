package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WidgetPreviewAdapter extends BaseAdapterDelegate<WidgetPreviewAdapter.ViewHolder, WidgetPreview> {

    private final Picasso picasso;
    private final Listener listener;

    public WidgetPreviewAdapter(Picasso picasso, Listener listener) {
        this.picasso = picasso;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_widget_preview;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onWidgetSelected(getItem(position).info);
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, WidgetPreview item) {
        holder.bind(item);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof WidgetPreview;
    }

    @Override
    public long getItemId(int position, WidgetPreview item) {
        return item.info.provider.hashCode();
    }

    public interface Listener {
        void onWidgetSelected(AppWidgetProviderInfo info);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imagePreview;
        final ImageView imageIcon;
        final TextView textName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePreview = itemView.findViewById(R.id.widget_preview);
            imageIcon = itemView.findViewById(R.id.widget_icon);
            textName = itemView.findViewById(R.id.widget_name);
        }

        void bind(WidgetPreview info) {
            textName.setText(info.label);
            picasso.load(info.iconUri)
                    .fit()
                    .into(imageIcon);
            picasso.load(info.previewUri)
                    .into(imagePreview);
        }
    }
}
