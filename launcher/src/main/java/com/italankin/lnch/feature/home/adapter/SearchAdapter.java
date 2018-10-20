package com.italankin.lnch.feature.home.adapter;

import android.net.Uri;
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
import com.italankin.lnch.model.repository.search.match.Match;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private final Picasso picasso;
    private final Filter filter;
    private final Listener listener;

    private List<? extends Match> dataset = new ArrayList<>(0);

    public SearchAdapter(Picasso picasso, SearchRepository searchRepository, Listener listener) {
        this.picasso = picasso;
        this.filter = new SearchFilter(searchRepository);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    @Override
    public Match getItem(int position) {
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
        Match item = getItem(position);
        holder.text.setText(item.getLabel());
        holder.text.setTextColor(item.getColor());

        Uri icon = item.getIcon();
        if (icon != null) {
            picasso.load(icon)
                    .fit()
                    .into(holder.image);
        } else {
            picasso.cancelRequest(holder.image);
            holder.image.setImageResource(item.getIconResource());
        }

        if (listener != null) {
            convertView.setOnClickListener(v -> {
                listener.onItemClick(position, item);
            });
            convertView.setOnLongClickListener(v -> {
                listener.onItemLongClick(position, item);
                return true;
            });
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

    public interface Listener {
        void onItemClick(int position, Match match);

        void onItemLongClick(int position, Match match);
    }

    class SearchFilter extends Filter {
        private final SearchRepository searchRepository;

        public SearchFilter(SearchRepository searchRepository) {
            this.searchRepository = searchRepository;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<? extends Match> matches = searchRepository.search(constraint);
            results.values = matches;
            results.count = matches.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            dataset = (List<Match>) results.values;
            notifyDataSetChanged();
        }
    }
}

