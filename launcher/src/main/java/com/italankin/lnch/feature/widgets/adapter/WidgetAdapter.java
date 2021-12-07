package com.italankin.lnch.feature.widgets.adapter;

import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;

import androidx.annotation.NonNull;

public class WidgetAdapter extends AbstractWidgetAdapter<WidgetAdapter.WidgetViewHolder> {

    private final LauncherAppWidgetHost appWidgetHost;
    private final OnWidgetLongClickListener longClickListener;

    public WidgetAdapter(LauncherAppWidgetHost appWidgetHost, OnWidgetLongClickListener longClickListener) {
        this.appWidgetHost = appWidgetHost;
        this.longClickListener = longClickListener;
    }

    @Override
    public WidgetViewHolder onCreate(AppWidget item) {
        LauncherAppWidgetHostView view = appWidgetHost.createView(item.appWidgetId, item.providerInfo);
        return new WidgetViewHolder(view);
    }

    @Override
    public void onBind(WidgetViewHolder holder, int position, AppWidget item) {
        holder.hostView.setDimensionsConstraints(
                item.minWidth, item.minHeight, item.maxWidth, item.maxHeight
        );
        holder.hostView.updateAppWidgetOptions(item.options);
        holder.hostView.setOnLongClickListener(v -> {
            return longClickListener.onWidgetLongClick(item.appWidgetId, holder.hostView);
        });
    }

    static class WidgetViewHolder extends AbstractWidgetAdapter.ViewHolder {

        final LauncherAppWidgetHostView hostView;

        WidgetViewHolder(@NonNull LauncherAppWidgetHostView itemView) {
            super(itemView);
            this.hostView = itemView;
        }
    }

    public interface OnWidgetLongClickListener {
        boolean onWidgetLongClick(int appWidgetId, LauncherAppWidgetHostView hostView);
    }
}
