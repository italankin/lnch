package com.italankin.lnch.feature.widgets.gallery;

import com.italankin.lnch.util.SearchUtils;
import com.italankin.lnch.util.filter.ListFilter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

class WidgetFilter extends ListFilter<WidgetPreview> {

    public WidgetFilter(@Nullable OnFilterResult<WidgetPreview> onFilterResult) {
        super(onFilterResult);
    }

    @Override
    protected FilterResults performFiltering(String query, List<WidgetPreview> unfiltered) {
        if (query.isEmpty()) {
            return of(unfiltered);
        }
        List<WidgetPreview> result = new ArrayList<>(unfiltered.size());
        for (WidgetPreview preview : unfiltered) {
            if (SearchUtils.contains(preview.appName, query)) {
                result.add(preview);
            }
        }
        return of(result);
    }
}
