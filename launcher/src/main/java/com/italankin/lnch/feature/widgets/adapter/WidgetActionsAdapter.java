package com.italankin.lnch.feature.widgets.adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.model.WidgetActionsItem;
import me.italankin.adapterdelegates.BaseAdapterDelegate;

public class WidgetActionsAdapter extends BaseAdapterDelegate<WidgetActionsAdapter.AddWidgetViewHolder, WidgetActionsItem> {

    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    public WidgetActionsAdapter(View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_widget_actions;
    }

    @NonNull
    @Override
    protected AddWidgetViewHolder createViewHolder(View itemView) {
        AddWidgetViewHolder viewHolder = new AddWidgetViewHolder(itemView);
        viewHolder.addWidget.setOnClickListener(onClickListener);
        viewHolder.addWidget.setOnLongClickListener(onLongClickListener);
        return viewHolder;
    }

    @Override
    public void onBind(AddWidgetViewHolder holder, int position, WidgetActionsItem item) {
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof WidgetActionsItem;
    }

    static class AddWidgetViewHolder extends RecyclerView.ViewHolder {

        final View addWidget;

        AddWidgetViewHolder(@NonNull View itemView) {
            super(itemView);
            addWidget = itemView.findViewById(R.id.add_widget);
        }
    }
}
