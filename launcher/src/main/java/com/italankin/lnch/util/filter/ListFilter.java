package com.italankin.lnch.util.filter;

import android.text.TextUtils;
import android.widget.Filter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.emptyList;

public abstract class ListFilter<T> extends Filter {
    protected static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.count = 0;
        EMPTY.values = emptyList();
    }

    protected final List<T> unfiltered = new CopyOnWriteArrayList<>();
    @Nullable
    protected final OnFilterResult<T> onFilterResult;

    protected volatile CharSequence constraint;

    public ListFilter(@Nullable OnFilterResult<T> onFilterResult) {
        this.onFilterResult = onFilterResult;
    }

    public void setDataset(@NonNull List<T> dataset) {
        unfiltered.clear();
        unfiltered.addAll(dataset);
        fireFilter();
    }

    protected abstract FilterResults performFiltering(String query, List<T> unfiltered);

    @Override
    protected final FilterResults performFiltering(CharSequence constraint) {
        this.constraint = constraint;
        String query = "";
        if (constraint != null) {
            query = constraint.toString().trim().toLowerCase(Locale.getDefault());
        }
        if (query.isEmpty()) {
            return emptyConstraintResults();
        }
        return performFiltering(query, unfiltered);
    }

    protected FilterResults emptyConstraintResults() {
        return of(unfiltered);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void publishResults(CharSequence constraint, FilterResults filterResults) {
        if (onFilterResult != null) {
            onFilterResult.onFilterResult(constraint == null ? null : constraint.toString(),
                    (List<T>) filterResults.values);
        }
    }

    protected void fireFilter() {
        if (!TextUtils.isEmpty(constraint)) {
            filter(constraint);
        } else {
            publishResults(null, of(new ArrayList<>(unfiltered)));
        }
    }

    protected static <T> FilterResults of(List<T> items) {
        if (items.size() == 0) {
            return EMPTY;
        }
        FilterResults results = new FilterResults();
        results.values = items;
        results.count = items.size();
        return results;
    }

    public interface OnFilterResult<T> {
        void onFilterResult(String query, List<T> items);
    }
}
