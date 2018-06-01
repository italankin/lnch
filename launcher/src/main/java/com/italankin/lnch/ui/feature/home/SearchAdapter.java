package com.italankin.lnch.ui.feature.home;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.IMatch;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private final Filter filter;

    private List<? extends IMatch> dataset = new ArrayList<>(0);

    public SearchAdapter(SearchRepository searchRepository) {
        this.filter = new SearchFilter(searchRepository) {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataset = (List<IMatch>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    @Override
    public IMatch getItem(int position) {
        return dataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dataset.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_search_match, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        IMatch item = getItem(position);
        holder.text.setText(item.getLabel());
        holder.text.setTextColor(item.getColor());
        Drawable icon = item.getIcon();
        if (icon != null) {
            holder.image.setImageDrawable(icon);
        } else {
            holder.image.setImageResource(item.getIconResource());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class ViewHolder {
        final TextView text;
        final ImageView image;

        public ViewHolder(View itemView) {
            this.text = itemView.findViewById(R.id.text);
            this.image = itemView.findViewById(R.id.image);
        }
    }
}

abstract class SearchFilter extends Filter {
    private final SearchRepository searchRepository;

    public SearchFilter(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        List<? extends IMatch> matches = searchRepository.search(constraint);
        results.values = matches;
        results.count = matches.size();
        return results;
    }
}
