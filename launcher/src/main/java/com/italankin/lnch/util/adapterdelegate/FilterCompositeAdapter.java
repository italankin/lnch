package com.italankin.lnch.util.adapterdelegate;

import android.content.Context;
import android.util.SparseArray;
import android.widget.Filter;
import android.widget.Filterable;

public class FilterCompositeAdapter<T> extends CompositeAdapter<T> implements Filterable {

    private final Filter filter;

    private FilterCompositeAdapter(Context context, SparseArray<AdapterDelegate> delegates,
            boolean hasStableIds, Filter filter) {
        super(context, delegates, hasStableIds);
        this.filter = filter;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void filter(CharSequence constaint) {
        filter.filter(constaint);
    }

    public static class Builder<T> extends BaseBuilder<T, Builder<T>> {
        private Filter filter;

        public Builder(Context context) {
            super(context);
        }

        public Builder<T> filter(Filter filter) {
            this.filter = filter;
            return this;
        }

        @Override
        protected CompositeAdapter<T> createAdapter() {
            if (filter == null) {
                throw new NullPointerException("filter cannot be null");
            }
            return new FilterCompositeAdapter<>(context, delegates, hasStableIds, filter);
        }
    }
}
