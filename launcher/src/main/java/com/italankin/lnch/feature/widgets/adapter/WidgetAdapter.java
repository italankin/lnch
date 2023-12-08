package com.italankin.lnch.feature.widgets.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;

import java.util.Collections;
import java.util.List;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder> {

    private List<AppWidget> items = Collections.emptyList();
    private final LauncherAppWidgetHost appWidgetHost;
    private final WidgetResizeFrame.OnStartDragListener onStartDragListener;
    private final WidgetActionListener widgetActionListener;
    private final int cellSize;

    public WidgetAdapter(LauncherAppWidgetHost appWidgetHost,
            int cellSize,
            WidgetResizeFrame.OnStartDragListener onStartDragListener,
            WidgetActionListener widgetActionListener) {
        this.appWidgetHost = appWidgetHost;
        this.cellSize = cellSize;
        this.onStartDragListener = onStartDragListener;
        this.widgetActionListener = widgetActionListener;
    }

    public void setItems(List<AppWidget> items) {
        this.items = items == null ? Collections.emptyList() : items;
    }

    @NonNull
    @Override
    public WidgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppWidget item = findItemById(viewType);
        WidgetResizeFrame resizeFrame = new WidgetResizeFrame(parent.getContext());
        resizeFrame.setLayoutParams(new RecyclerView.LayoutParams(item.size.width, item.size.height));
        LauncherAppWidgetHostView hostView = appWidgetHost.createView(item.appWidgetId, item.providerInfo);
        resizeFrame.bindAppWidget(item, hostView);
        resizeFrame.setCellSize(cellSize);
        resizeFrame.setDeleteAction(v -> widgetActionListener.onWidgetDelete(item));
        resizeFrame.setConfigureAction(v -> widgetActionListener.onWidgetReconfigure(item));
        resizeFrame.setOnStartDragListener(onStartDragListener);
        WidgetViewHolder holder = new WidgetViewHolder(resizeFrame);
        resizeFrame.setCommitAction(() -> notifyDataSetChanged());
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).appWidgetId;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).appWidgetId;
    }

    public AppWidget getItem(int position) {
        return items.get(position);
    }

    @Override
    public void onBindViewHolder(WidgetAdapter.WidgetViewHolder holder, int position) {
        AppWidget item = items.get(position);
        holder.resizeFrame.setResizeMode(item.resizeMode, false);
        holder.resizeFrame.setForceResize(item.forceResize);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetAdapter.WidgetViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            AppWidget item = items.get(position);
            holder.resizeFrame.setResizeMode(item.resizeMode, true);
            holder.resizeFrame.setForceResize(item.forceResize);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private AppWidget findItemById(int id) {
        for (AppWidget item : items) {
            if (item.appWidgetId == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("No item found for id=" + id);
    }

    public static class WidgetViewHolder extends RecyclerView.ViewHolder {

        public final WidgetResizeFrame resizeFrame;

        WidgetViewHolder(WidgetResizeFrame resizeFrame) {
            super(resizeFrame);
            this.resizeFrame = resizeFrame;
            setIsRecyclable(false);
        }
    }

    public interface WidgetActionListener {

        void onWidgetReconfigure(AppWidget appWidget);

        void onWidgetDelete(AppWidget appWidget);
    }
}
