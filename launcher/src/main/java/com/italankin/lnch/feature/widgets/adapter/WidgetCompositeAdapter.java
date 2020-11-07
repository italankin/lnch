package com.italankin.lnch.feature.widgets.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.WidgetAdapterItem;
import com.italankin.lnch.util.adapterdelegate.AdapterDelegate;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"unchecked", "rawtypes"})
public class WidgetCompositeAdapter extends CompositeAdapter<WidgetAdapterItem> {

    private static final int KEY_WIDGET_ADAPTER = 2 << 5;

    private static final int FLAG_WIDGET_TYPE = 1 << 30;
    private static final int WIDGET_POSITION_MASK = 0x0000ffff;

    protected WidgetCompositeAdapter(Context context, SparseArray<AdapterDelegate> delegates, boolean hasStableIds) {
        super(context, delegates, hasStableIds);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if ((FLAG_WIDGET_TYPE & viewType) != FLAG_WIDGET_TYPE) {
            return super.onCreateViewHolder(parent, viewType);
        } else {
            int position = viewType & WIDGET_POSITION_MASK;
            AppWidget item = (AppWidget) getItem(position);
            return getWidgetAdapterDelegate().onCreate(item);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WidgetAdapterItem item = getItem(position);
        if (item instanceof AppWidget) {
            getWidgetAdapterDelegate().onBind(holder, position, ((AppWidget) item));
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        WidgetAdapterItem item = getItem(position);
        if (item instanceof AppWidget) {
            getWidgetAdapterDelegate().onBind(holder, position, ((AppWidget) item), payloads);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        WidgetAdapterItem item = getItem(position);
        if (item instanceof AppWidget) {
            return position | FLAG_WIDGET_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        if ((FLAG_WIDGET_TYPE & viewType) != FLAG_WIDGET_TYPE) {
            super.onViewRecycled(holder);
        } else {
            getWidgetAdapterDelegate().onRecycled(holder);
        }
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        if ((FLAG_WIDGET_TYPE & viewType) != FLAG_WIDGET_TYPE) {
            return super.onFailedToRecycleView(holder);
        } else {
            return getWidgetAdapterDelegate().onFailedToRecycle(holder);
        }
    }

    private AbstractWidgetAdapter<? super RecyclerView.ViewHolder> getWidgetAdapterDelegate() {
        return (AbstractWidgetAdapter<? super RecyclerView.ViewHolder>) delegates.get(KEY_WIDGET_ADAPTER);
    }

    public static class Builder extends BaseBuilder<WidgetAdapterItem, Builder> {

        public Builder(Context context) {
            super(context);
        }

        public Builder add(AbstractWidgetAdapter<?> delegate) {
            return super.add(KEY_WIDGET_ADAPTER, delegate);
        }

        @Override
        public Builder add(int viewType, AdapterDelegate<?, ? extends WidgetAdapterItem> delegate) {
            if (viewType == KEY_WIDGET_ADAPTER) {
                throw new IllegalArgumentException("viewType=" + viewType + " is reserved");
            }
            return super.add(viewType, delegate);
        }

        @Override
        protected WidgetCompositeAdapter createAdapter() {
            return new WidgetCompositeAdapter(context, delegates, hasStableIds);
        }

        @Override
        public WidgetCompositeAdapter create() {
            return (WidgetCompositeAdapter) super.create();
        }
    }
}
