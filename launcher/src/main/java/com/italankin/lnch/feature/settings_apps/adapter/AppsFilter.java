package com.italankin.lnch.feature.settings_apps.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Filter;

import com.italankin.lnch.feature.settings_apps.model.DecoratedAppViewModel;
import com.italankin.lnch.util.SearchUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class AppsFilter extends Filter {
    public static final int FLAG_HIDDEN = 1;
    public static final int FLAG_VISIBLE = 1 << 1;

    private static final int DEFAULT_FLAGS = FLAG_HIDDEN | FLAG_VISIBLE;
    private static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.count = 0;
        EMPTY.values = Collections.emptyList();
    }

    @NonNull
    private final List<DecoratedAppViewModel> unfiltered;
    @Nullable
    private final OnFilterResult onFilterResult;

    private volatile int flags = DEFAULT_FLAGS;
    private volatile CharSequence constraint;

    public AppsFilter(@NonNull List<DecoratedAppViewModel> items, @Nullable OnFilterResult onFilterResult) {
        this.unfiltered = items;
        this.onFilterResult = onFilterResult;
    }

    public void setFlags(int newFlags) {
        if (flags != newFlags) {
            flags = newFlags;
            filter(constraint);
        }
    }

    public void resetFlags() {
        setFlags(DEFAULT_FLAGS);
    }

    public int getFlags() {
        return flags;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        this.constraint = constraint;
        List<DecoratedAppViewModel> result = new ArrayList<>(unfiltered.size());
        for (DecoratedAppViewModel item : unfiltered) {
            if (!item.isHidden() && (flags & FLAG_VISIBLE) == 0) {
                continue;
            }
            if (item.isHidden() && (flags & FLAG_HIDDEN) == 0) {
                continue;
            }
            result.add(item);
        }
        if (TextUtils.isEmpty(constraint)) {
            return of(result);
        }
        String query = constraint.toString().trim().toLowerCase(Locale.getDefault());
        Iterator<DecoratedAppViewModel> iterator = result.iterator();
        while (iterator.hasNext()) {
            DecoratedAppViewModel item = iterator.next();
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
                    (List<DecoratedAppViewModel>) filterResults.values);
        }
    }

    private static FilterResults of(List<DecoratedAppViewModel> items) {
        if (items.size() == 0) {
            return EMPTY;
        }
        FilterResults results = new FilterResults();
        results.values = items;
        results.count = items.size();
        return results;
    }

    public interface OnFilterResult {
        void onFilterResult(String query, List<DecoratedAppViewModel> items);
    }
}
