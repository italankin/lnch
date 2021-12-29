package com.italankin.lnch.feature.home.search;

import android.widget.Filter;

import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.Match;

import java.util.List;

import static java.util.Collections.emptyList;

class SearchResultsFilter extends Filter {

    private static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.count = 0;
        EMPTY.values = emptyList();
    }

    private final SearchRepository searchRepository;
    private final Listener listener;

    SearchResultsFilter(SearchRepository searchRepository, Listener listener) {
        this.searchRepository = searchRepository;
        this.listener = listener;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return of(searchRepository.recent());
        }
        List<? extends Match> results = searchRepository.search(constraint);
        return of(results);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results != null) {
            listener.onSearchResults((List<Match>) results.values);
        }
    }

    private static <T> FilterResults of(List<T> items) {
        if (items.size() == 0) {
            return EMPTY;
        }
        FilterResults results = new FilterResults();
        results.values = items;
        results.count = items.size();
        return results;
    }

    interface Listener {
        void onSearchResults(List<Match> results);
    }
}
