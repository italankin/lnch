package com.italankin.lnch.feature.widgets.adapter;

import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.model.AddWidget;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AddWidgetAdapter extends BaseAdapterDelegate<AddWidgetAdapter.AddWidgetViewHolder, AddWidget> {

    private final View.OnClickListener onClickListener;

    public AddWidgetAdapter(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_add_widget;
    }

    @NonNull
    @Override
    protected AddWidgetViewHolder createViewHolder(View itemView) {
        AddWidgetViewHolder viewHolder = new AddWidgetViewHolder(itemView);
        viewHolder.addWidget.setOnClickListener(onClickListener);
        return viewHolder;
    }

    @Override
    public void onBind(AddWidgetViewHolder holder, int position, AddWidget item) {
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AddWidget;
    }

    static class AddWidgetViewHolder extends RecyclerView.ViewHolder {

        final View addWidget;

        AddWidgetViewHolder(@NonNull View itemView) {
            super(itemView);
            addWidget = itemView.findViewById(R.id.add_widget);
        }
    }
}
