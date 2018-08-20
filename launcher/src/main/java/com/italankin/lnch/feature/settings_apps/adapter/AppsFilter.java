package com.italankin.lnch.feature.settings_apps.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Filter;

import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.util.SearchUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AppsFilter extends Filter {
    private static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.count = 0;
        EMPTY.values = Collections.emptyList();
    }

    @NonNull
    private final List<AppViewModel> unfiltered;
    @Nullable
    private final OnFilterResult onFilterResult;

    public AppsFilter(@NonNull List<AppViewModel> items, @Nullable OnFilterResult onFilterResult) {
        this.unfiltered = items;
        this.onFilterResult = onFilterResult;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return of(unfiltered);
        }
        String query = constraint.toString().trim().toLowerCase();
        List<AppViewModel> result = new ArrayList<>(unfiltered.size());
        for (AppViewModel item : unfiltered) {
            if (SearchUtils.contains(item.item.label, query) ||
                    SearchUtils.contains(item.item.customLabel, query)) {
                result.add(item);
            }
        }
        return of(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        if (onFilterResult != null) {
            onFilterResult.onFilterResult(constraint == null ? null : constraint.toString(),
                    (List<AppViewModel>) filterResults.values);
        }
    }

    private static FilterResults of(List<AppViewModel> items) {
        if (items.size() == 0) {
            return EMPTY;
        }
        FilterResults results = new FilterResults();
        results.values = items;
        results.count = items.size();
        return results;
    }

    public interface OnFilterResult {
        void onFilterResult(String query, List<AppViewModel> items);
    }
}
