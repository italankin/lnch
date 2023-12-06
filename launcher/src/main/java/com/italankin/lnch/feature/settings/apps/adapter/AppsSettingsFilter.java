package com.italankin.lnch.feature.settings.apps.adapter;

import android.text.TextUtils;
import com.italankin.lnch.feature.settings.apps.model.FilterFlag;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.search.Searchable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableSet;

public class AppsSettingsFilter extends ListFilter<AppDescriptorUi> {
    private static final Set<FilterFlag> DEFAULT_FLAGS = unmodifiableSet(EnumSet.allOf(FilterFlag.class));

    private final Set<FilterFlag> flags = synchronizedSet(EnumSet.copyOf(DEFAULT_FLAGS));

    public AppsSettingsFilter(OnFilterResult<AppDescriptorUi> onFilterResult) {
        super(onFilterResult);
    }

    public void setFlags(Set<FilterFlag> newFlags) {
        if (!flags.equals(newFlags)) {
            flags.clear();
            flags.addAll(newFlags);
            fireFilter();
        }
    }

    public void resetFlags() {
        setFlags(DEFAULT_FLAGS);
    }

    public EnumSet<FilterFlag> getFlags() {
        return flags.isEmpty() ? EnumSet.noneOf(FilterFlag.class) : EnumSet.copyOf(flags);
    }

    @Override
    protected FilterResults performFiltering(String query, boolean caseSensitive, List<AppDescriptorUi> unfiltered) {
        List<AppDescriptorUi> result = filterByFlags(unfiltered);
        Iterator<AppDescriptorUi> iterator = result.iterator();
        while (iterator.hasNext()) {
            AppDescriptorUi item = iterator.next();
            Searchable searchable = () -> Searchable.createTokens(
                    item.getLabel(),
                    item.getCustomLabel(),
                    item.packageName
            );
            if (!searchable.matches(query)) {
                iterator.remove();
            }
        }
        return of(result);
    }

    @NotNull
    private List<AppDescriptorUi> filterByFlags(List<AppDescriptorUi> unfiltered) {
        boolean includeVisible = flags.contains(FilterFlag.VISIBLE);
        boolean includeIgnored = flags.contains(FilterFlag.IGNORED);
        List<AppDescriptorUi> result = new ArrayList<>(unfiltered.size());
        for (AppDescriptorUi item : unfiltered) {
            if (!item.isIgnored() && !includeVisible) {
                continue;
            }
            if (item.isIgnored() && !includeIgnored) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    @Override
    protected FilterResults emptyConstraintResults() {
        return of(filterByFlags(unfiltered));
    }

    protected void fireFilter() {
        if (!TextUtils.isEmpty(constraint) || !DEFAULT_FLAGS.equals(flags)) {
            filter(constraint);
        } else {
            publishResults(null, of(new ArrayList<>(unfiltered)));
        }
    }
}
