package com.italankin.lnch.feature.widgets.adapter;

import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;

import java.util.List;

public class WidgetAdapter extends AbstractWidgetAdapter<WidgetAdapter.WidgetViewHolder> {

    private final LauncherAppWidgetHost appWidgetHost;
    private final WidgetResizeFrame.OnStartDragListener onStartDragListener;
    private final Listener listener;
    private final int cellSize;

    public WidgetAdapter(LauncherAppWidgetHost appWidgetHost,
            int cellSize,
            WidgetResizeFrame.OnStartDragListener onStartDragListener,
            Listener listener) {
        this.cellSize = cellSize;
        this.onStartDragListener = onStartDragListener;
        this.listener = listener;
        this.appWidgetHost = appWidgetHost;
    }

    @Override
    public WidgetViewHolder onCreate(AppWidget item, ViewGroup parent) {
        LauncherAppWidgetHostView hostView = appWidgetHost.createView(item.appWidgetId, item.providerInfo);
        WidgetResizeFrame resizeFrame = new WidgetResizeFrame(parent.getContext());
        resizeFrame.setCellSize(cellSize);
        MarginLayoutParams lp = new MarginLayoutParams(item.size.width, item.size.height);
        resizeFrame.setLayoutParams(lp);
        resizeFrame.addView(hostView, 0, new LayoutParams(item.size.width, item.size.height));
        resizeFrame.setDeleteAction(v -> listener.onWidgetDelete(item));
        resizeFrame.setConfigureAction(v -> listener.onWidgetConfigure(item));
        resizeFrame.setOnStartDragListener(onStartDragListener);
        resizeFrame.bindAppWidget(item);
        WidgetViewHolder holder = new WidgetViewHolder(resizeFrame);
        resizeFrame.setCommitAction(() -> adapter.notifyDataSetChanged());
        return holder;
    }

    @Override
    public void onBind(WidgetViewHolder holder, int position, AppWidget item) {
        holder.resizeFrame.setResizeMode(item.resizeMode);
        holder.resizeFrame.setForceResize(item.forceResize);
    }

    @Override
    public void onBind(WidgetViewHolder holder, int position, AppWidget item, List<?> payloads) {
        if (payloads.isEmpty()) {
            super.onBind(holder, position, item, payloads);
        }
    }

    public static class WidgetViewHolder extends AbstractWidgetAdapter.ViewHolder {

        final WidgetResizeFrame resizeFrame;

        WidgetViewHolder(WidgetResizeFrame resizeFrame) {
            super(resizeFrame);
            this.resizeFrame = resizeFrame;
        }
    }

    public interface Listener {

        void onWidgetConfigure(AppWidget appWidget);

        void onWidgetDelete(AppWidget appWidget);
    }
}
