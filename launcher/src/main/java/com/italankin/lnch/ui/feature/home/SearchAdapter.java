package com.italankin.lnch.ui.feature.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.PackageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends BaseAdapter implements Filterable {
    private final Filter filter;
    private List<PackageModel> filtered = new ArrayList<>(0);

    public SearchAdapter(List<PackageModel> dataset) {
        this.filter = new SearchFilter(dataset) {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                filtered = (List<PackageModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Override
    public PackageModel getItem(int position) {
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
        PackageModel item = getItem(position);
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

    private final List<PackageModel> dataset;

    public SearchFilter(List<PackageModel> dataset) {
        this.dataset = dataset;
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
        List<PackageModel> values = new ArrayList<>(8);
        List<PackageModel> matchCustomLabel = new ArrayList<>(2);
        List<PackageModel> matchLabel = new ArrayList<>(2);
        List<PackageModel> matchPackageName = new ArrayList<>(2);
        for (PackageModel item : dataset) {
            if (startsWith(item.customLabel, s) || startsWith(item.label, s)) {
                values.add(item);
                continue;
            }
            if (contains(item.customLabel, s)) {
                matchCustomLabel.add(item);
                continue;
            }
            if (contains(item.label, s)) {
                matchLabel.add(item);
                continue;
            }
            if (contains(item.packageName, s)) {
                matchPackageName.add(item);
            }
        }
        values.addAll(matchCustomLabel);
        values.addAll(matchLabel);
        values.addAll(matchPackageName);
        results.values = values;
        results.count = values.size();
        return results;
    }

    private boolean contains(String s1, String s2) {
        return s1 != null && s2 != null && s1.toLowerCase(Locale.getDefault())
                .contains(s2.toLowerCase(Locale.getDefault()));
    }

    private boolean startsWith(String s1, String s2) {
        return s1 != null && s2 != null && s1.toLowerCase(Locale.getDefault())
                .startsWith(s2.toLowerCase(Locale.getDefault()));
    }
}