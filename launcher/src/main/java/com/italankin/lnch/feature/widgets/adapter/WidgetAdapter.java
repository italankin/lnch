package com.italankin.lnch.feature.widgets.adapter;

import android.view.ViewGroup;

import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class WidgetAdapter extends ListAdapter<AppWidget, WidgetAdapter.WidgetViewHolder> {

    private final LauncherAppWidgetHost appWidgetHost;
    private final OnWidgetLongClickListener longClickListener;

    public WidgetAdapter(LauncherAppWidgetHost appWidgetHost, OnWidgetLongClickListener longClickListener) {
        super(new WidgetDiffUtilCallback());
        this.appWidgetHost = appWidgetHost;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public WidgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppWidget appWidget = getItem(viewType);
        return new WidgetViewHolder(appWidgetHost.createView(appWidget.appWidgetId, appWidget.providerInfo));
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetViewHolder holder, int position) {
        AppWidget appWidget = getItem(position);
        holder.hostView.setDimensionsConstraints(
                appWidget.minWidth, appWidget.minHeight, appWidget.maxWidth, appWidget.maxHeight
        );
        holder.hostView.updateAppWidgetOptions(appWidget.options);
        holder.hostView.setOnLongClickListener(v -> {
            return longClickListener.onWidgetLongClick(appWidget.appWidgetId, holder.hostView);
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).appWidgetId;
    }

    static class WidgetViewHolder extends RecyclerView.ViewHolder {

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
