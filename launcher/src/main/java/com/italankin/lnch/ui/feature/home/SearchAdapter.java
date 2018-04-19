package com.italankin.lnch.ui.feature.home;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.searchable.ISearchable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {
    private final Filter filter;
    private List<? extends ISearchable> filtered = new ArrayList<>(0);

    public SearchAdapter(List<? extends ISearchable> dataset, List<? extends ISearchable> fallbacks) {
        this.filter = new SearchFilter(dataset, fallbacks) {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (List<ISearchable>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Override
    public ISearchable getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return filtered.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_searchable, parent, false);
            holder = new ViewHolder((TextView) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ISearchable item = getItem(position);
        holder.text.setText(item.getLabel());
        holder.text.setTextColor(item.getColor());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class ViewHolder {
        final TextView text;

        public ViewHolder(TextView text) {
            this.text = text;
        }
    }
}

abstract class SearchFilter extends Filter {
    private static final FilterResults EMPTY;

    static {
        EMPTY = new FilterResults();
        EMPTY.values = Collections.emptyList();
        EMPTY.count = 0;
    }

    private final List<? extends ISearchable> dataset;
    private final List<? extends ISearchable> fallbacks;

    public SearchFilter(List<? extends ISearchable> dataset, List<? extends ISearchable> fallbacks) {
        this.dataset = dataset;
        this.fallbacks = fallbacks;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint == null) {
            results.values = new ArrayList<>(dataset);
            results.count = dataset.size();
            return results;
        }
        if (constraint.length() == 0) {
            return EMPTY;
        }
        String s = constraint.toString();
        List<Matched> matched = new ArrayList<>(dataset.size());
        // search apps
        for (ISearchable item : dataset) {
            ISearchable.Match match = item.filter(s);
            if (match != ISearchable.Match.NONE) {
                matched.add(new Matched(item, match));
            }
        }
        // search other sources
        for (ISearchable fallback : fallbacks) {
            ISearchable.Match match = fallback.filter(s);
            if (match != ISearchable.Match.NONE) {
                matched.add(new Matched(fallback, match));
            }
        }
        Collections.sort(matched);
        List<ISearchable> result = new ArrayList<>(matched.size());
        for (Matched m : matched) {
            result.add(m.what);
        }
        results.values = result;
        results.count = result.size();
        return results;
    }
}

class Matched implements Comparable<Matched> {
    ISearchable what;
    ISearchable.Match by;

    Matched(ISearchable what, ISearchable.Match by) {
        this.by = by;
        this.what = what;
    }

    @Override
    public int compareTo(@NonNull Matched o) {
        return by.compareTo(o.by);
    }
}