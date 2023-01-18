package com.italankin.lnch.util.filter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SimpleListFilter<T> extends ListFilter<T> {

    private final ApplyFilter<T> applyFilter;

    public SimpleListFilter(@Nullable OnFilterResult<T> onFilterResult, ApplyFilter<T> applyFilter) {
        super(onFilterResult);
        this.applyFilter = applyFilter;
    }

    @Override
    protected FilterResults performFiltering(String query, List<T> unfiltered) {
        List<T> filtered = new ArrayList<>(unfiltered.size());
        for (T item : unfiltered) {
            if (applyFilter.apply(query, item)) {
                filtered.add(item);
            }
        }
        return of(filtered);
    }

    public interface ApplyFilter<T> {

        boolean apply(String query, T item);
    }
}
