package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetProviderInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import me.italankin.adapterdelegates.BaseAdapterDelegate;
import com.italankin.lnch.util.imageloader.ImageLoader;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WidgetPreviewAdapter extends BaseAdapterDelegate<WidgetPreviewAdapter.ViewHolder, WidgetPreview> {

    private final ImageLoader imageLoader;
    private final Listener listener;

    public WidgetPreviewAdapter(ImageLoader imageLoader, Listener listener) {
        this.imageLoader = imageLoader;
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
            imageLoader.load(info.iconUri)
                    .into(imageIcon);
            imageLoader.load(info.previewUri)
                    .noCache()
                    .into(imagePreview);
        }
    }
}
