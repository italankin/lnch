package com.italankin.lnch.feature.intentfactory.componentselector.adapter;

import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.italankin.lnch.util.filter.ListFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public class ComponentNameFilter extends ListFilter<ComponentNameUi> {

    public ComponentNameFilter(@Nullable OnFilterResult<ComponentNameUi> onFilterResult) {
        super(onFilterResult);
    }

    @Override
    protected FilterResults performFiltering(String query, List<ComponentNameUi> unfiltered) {
        List<ComponentNameUi> result = new ArrayList<>(unfiltered.size());
        for (ComponentNameUi item : unfiltered) {
            if (item.packageName.toLowerCase(Locale.ROOT).contains(query) ||
                    item.className.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(item);
            }
        }
        return of(result);
    }
}
