package com.italankin.lnch.util.filter;

import android.widget.Filter;
import androidx.annotation.NonNull;

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
    protected final OnFilterResult<T> onFilterResult;
    protected final boolean caseSensitive;

    /**
     * Unmodified user input
     */
    protected volatile CharSequence rawConstraint;
    /**
     * Sanitized (no whitespace, lowercase if not {@link #caseSensitive}) user input
     */
    protected volatile CharSequence constraint = "";

    public ListFilter(OnFilterResult<T> onFilterResult) {
        this(onFilterResult, false);
    }

    public ListFilter(OnFilterResult<T> onFilterResult, boolean caseSensitive) {
        this.onFilterResult = onFilterResult;
        this.caseSensitive = caseSensitive;
    }

    public void setDataset(@NonNull List<T> dataset) {
        unfiltered.clear();
        unfiltered.addAll(dataset);
        fireFilter();
    }

    protected abstract FilterResults performFiltering(String query, boolean caseSensitive, List<T> unfiltered);

    @Override
    protected final FilterResults performFiltering(CharSequence constraint) {
        this.rawConstraint = constraint;
        String query = "";
        if (constraint != null) {
            if (caseSensitive) {
                query = constraint.toString().trim();
            } else {
                query = constraint.toString().trim().toLowerCase(Locale.getDefault());
            }
        }
        this.constraint = query;
        if (query.isEmpty()) {
            return emptyConstraintResults();
        }
        return performFiltering(query, caseSensitive, unfiltered);
    }

    protected FilterResults emptyConstraintResults() {
        return of(unfiltered);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void publishResults(CharSequence constraint, FilterResults filterResults) {
        onFilterResult.onFilterResult(constraint == null ? null : constraint.toString(), (List<T>) filterResults.values);
    }

    protected void fireFilter() {
        if (constraint.length() > 0) {
            filter(constraint);
        } else {
            publishResults(null, of(new ArrayList<>(unfiltered)));
        }
    }

    protected static <T> FilterResults of(List<T> items) {
        if (items.isEmpty()) {
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
