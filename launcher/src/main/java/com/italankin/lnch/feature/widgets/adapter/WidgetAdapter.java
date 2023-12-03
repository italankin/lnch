package com.italankin.lnch.feature.widgets.adapter;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;
import timber.log.Timber;

public class WidgetAdapter extends AbstractWidgetAdapter<WidgetAdapter.WidgetViewHolder> {

    private final LauncherAppWidgetHost appWidgetHost;
    private final OnWidgetLongClickListener longClickListener;

    public WidgetAdapter(LauncherAppWidgetHost appWidgetHost, OnWidgetLongClickListener longClickListener) {
        this.appWidgetHost = appWidgetHost;
        this.longClickListener = longClickListener;
    }

    @Override
    public WidgetViewHolder onCreate(AppWidget item, ViewGroup parent) {
        LauncherAppWidgetHostView hostView = appWidgetHost.createView(item.appWidgetId, item.providerInfo);
        WidgetResizeFrame root = new WidgetResizeFrame(parent.getContext());
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        root.addView(hostView, new WidgetResizeFrame.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL));
        return new WidgetViewHolder(root, hostView);
    }

    @Override
    public void onBind(WidgetViewHolder holder, int position, AppWidget item) {
        holder.hostView.setDimensionsConstraints(item.minWidth, item.minHeight, item.maxWidth, item.maxHeight);
        try {
            holder.hostView.updateAppWidgetOptions(item.options);
        } catch (Exception e) {
            Timber.e(e, "failed AppWidget options update:");
        }
        holder.hostView.setOnLongClickListener(v -> {
            return longClickListener.onWidgetLongClick(item.appWidgetId, holder.hostView);
        });
    }

    static class WidgetViewHolder extends AbstractWidgetAdapter.ViewHolder {

        final LauncherAppWidgetHostView hostView;

        WidgetViewHolder(View itemView, LauncherAppWidgetHostView hostView) {
            super(itemView);
            this.hostView = hostView;
        }
    }

    public interface OnWidgetLongClickListener {
        boolean onWidgetLongClick(int appWidgetId, LauncherAppWidgetHostView hostView);
    }
}
