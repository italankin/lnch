package com.italankin.lnch.feature.widgets.adapter;

import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.WidgetAdapterItem;
import me.italankin.adapterdelegates.AdapterDelegate;
import me.italankin.adapterdelegates.CompositeAdapter;

@SuppressWarnings({"rawtypes"})
public class WidgetCompositeAdapter extends CompositeAdapter<WidgetAdapterItem> {

    private static final int KEY_WIDGET_ADAPTER = 2 << 5;

    private static final int FLAG_WIDGET_TYPE = 1 << 30;
    private static final int WIDGET_POSITION_MASK = 0x0000ffff;

    protected WidgetCompositeAdapter(Context context, SparseArrayCompat<AdapterDelegate> delegates, boolean hasStableIds) {
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
            return getWidgetAdapterDelegate().onCreate(item, parent);
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

    @NonNull
    @Override
    protected AdapterDelegate getDelegate(int position) {
        WidgetAdapterItem item = getItem(position);
        if (item instanceof AppWidget) {
            return getWidgetAdapterDelegate();
        } else {
            return super.getDelegate(position);
        }
    }

    private AbstractWidgetAdapter<?> getWidgetAdapterDelegate() {
        return (AbstractWidgetAdapter<?>) delegates.get(KEY_WIDGET_ADAPTER);
    }

    public static class Builder extends BaseBuilder<WidgetAdapterItem, Builder, WidgetCompositeAdapter> {

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
    }
}
