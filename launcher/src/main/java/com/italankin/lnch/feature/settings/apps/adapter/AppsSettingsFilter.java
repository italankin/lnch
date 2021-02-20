package com.italankin.lnch.feature.settings.apps.adapter;

import android.text.TextUtils;
import android.widget.Filter;

import com.italankin.lnch.feature.settings.apps.model.FilterFlag;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.SearchUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableSet;

public class AppsSettingsFilter extends Filter {
    private static final Set<FilterFlag> DEFAULT_FLAGS = unmodifiableSet(EnumSet.allOf(FilterFlag.class));
    private static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.count = 0;
        EMPTY.values = emptyList();
    }

    private final List<AppDescriptorUi> unfiltered = new CopyOnWriteArrayList<>();
    @Nullable
    private final OnFilterResult onFilterResult;

    private final Set<FilterFlag> flags = synchronizedSet(EnumSet.copyOf(DEFAULT_FLAGS));
    private volatile CharSequence constraint;

    public AppsSettingsFilter(@Nullable OnFilterResult onFilterResult) {
        this.onFilterResult = onFilterResult;
    }

    public void setDataset(@NonNull List<AppDescriptorUi> dataset) {
        unfiltered.clear();
        unfiltered.addAll(dataset);
        fireFilter();
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
    protected FilterResults performFiltering(CharSequence constraint) {
        this.constraint = constraint;

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
        if (TextUtils.isEmpty(constraint)) {
            return of(result);
        }
        String query = constraint.toString().trim().toLowerCase(Locale.getDefault());
        Iterator<AppDescriptorUi> iterator = result.iterator();
        while (iterator.hasNext()) {
            AppDescriptorUi item = iterator.next();
            if (!SearchUtils.contains(item.getDescriptor().label, query) &&
                    !SearchUtils.contains(item.getCustomLabel(), query) &&
                    !SearchUtils.contains(item.packageName, query)) {
                iterator.remove();
            }
        }
        return of(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        if (onFilterResult != null) {
            onFilterResult.onFilterResult(constraint == null ? null : constraint.toString(),
                    (List<AppDescriptorUi>) filterResults.values);
        }
    }

    private void fireFilter() {
        if (!TextUtils.isEmpty(constraint) || !DEFAULT_FLAGS.equals(flags)) {
            filter(constraint);
        } else {
            publishResults(null, of(new ArrayList<>(unfiltered)));
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

    public interface OnFilterResult {
        void onFilterResult(String query, List<AppDescriptorUi> items);
    }
}
