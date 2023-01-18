package com.italankin.lnch.feature.settings.hidden_items;

import com.italankin.lnch.util.SearchUtils;
import com.italankin.lnch.util.filter.ListFilter;

import java.util.ArrayList;
import java.util.List;

class HiddenItemsFilter extends ListFilter<HiddenItem> {

    public HiddenItemsFilter(OnFilterResult<HiddenItem> onFilterResult) {
        super(onFilterResult);
    }

    @Override
    protected FilterResults performFiltering(String query, List<HiddenItem> unfiltered) {
        List<HiddenItem> filtered = new ArrayList<>(unfiltered.size());
        for (HiddenItem hiddenItem : unfiltered) {
            if (SearchUtils.contains(hiddenItem.visibleLabel, query) ||
                    SearchUtils.contains(hiddenItem.originalLabel, query)) {
                filtered.add(hiddenItem);
            }
        }
        return of(filtered);
    }
}
