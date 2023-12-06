package com.italankin.lnch.util.filter;

import com.italankin.lnch.util.search.SearchUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedListFilter<T extends Searchable> extends ListFilter<T> {

    public SortedListFilter(OnFilterResult<T> onFilterResult) {
        super(onFilterResult);
    }

    @Override
    protected FilterResults performFiltering(String query, boolean ignored, List<T> unfiltered) {
        List<MatchingItem<T>> matchingItems = new ArrayList<>(unfiltered.size());
        for (T item : unfiltered) {
            Searchable.Match match = SearchUtils.match(item, query);
            if (match != null) {
                matchingItems.add(new MatchingItem<>(item, match));
            }
        }
        Collections.sort(matchingItems);
        List<T> filtered = new ArrayList<>(matchingItems.size());
        for (MatchingItem<T> matchingItem : matchingItems) {
            filtered.add(matchingItem.item);
        }
        return of(filtered);
    }

    private static class MatchingItem<T extends Searchable> implements Comparable<MatchingItem<T>> {
        final T item;
        final Searchable.Match match;

        MatchingItem(T item, Searchable.Match match) {
            this.item = item;
            this.match = match;
        }

        @Override
        public int compareTo(MatchingItem o) {
            return match.compareTo(o.match);
        }
    }
}
