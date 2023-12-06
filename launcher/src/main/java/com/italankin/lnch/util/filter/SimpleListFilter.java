package com.italankin.lnch.util.filter;

import com.italankin.lnch.util.search.Searchable;

import java.util.ArrayList;
import java.util.List;

public class SimpleListFilter<T> extends ListFilter<T> {

    public static <T extends Searchable> SimpleListFilter<T> createSearchable(OnFilterResult<T> onFilterResult) {
        return new SimpleListFilter<>(onFilterResult, (query, ignored, item) -> {
            return item.matches(query);
        });
    }

    private final ApplyFilter<T> applyFilter;

    public SimpleListFilter(OnFilterResult<T> onFilterResult, ApplyFilter<T> applyFilter) {
        this(onFilterResult, applyFilter, false);
    }

    public SimpleListFilter(OnFilterResult<T> onFilterResult, ApplyFilter<T> applyFilter, boolean caseSensitive) {
        super(onFilterResult, caseSensitive);
        this.applyFilter = applyFilter;
    }

    @Override
    protected FilterResults performFiltering(String query, boolean caseSensitive, List<T> unfiltered) {
        List<T> filtered = new ArrayList<>(unfiltered.size());
        for (T item : unfiltered) {
            if (applyFilter.apply(query, caseSensitive, item)) {
                filtered.add(item);
            }
        }
        return of(filtered);
    }

    public interface ApplyFilter<T> {

        boolean apply(String query, boolean caseSensitive, T item);
    }
}
