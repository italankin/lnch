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
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
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
            convertView.setOnClickListener(v -> {
                listener.onSearchItemClick(holder.adapterPosition, getItem(holder.adapterPosition));
            });
            holder.info.setOnClickListener(v -> {
                listener.onSearchItemInfoClick(holder.adapterPosition, getItem(holder.adapterPosition));
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Match item = getItem(position);
        holder.text.setText(item.getLabel());
        holder.text.setTextColor(item.getColor());
        holder.adapterPosition = position;

        Uri icon = item.getIcon();
        if (icon != null) {
            picasso.load(icon)
                    .fit()
                    .into(holder.image);
        } else {
            picasso.cancelRequest(holder.image);
            holder.image.setImageResource(item.getIconResource());
        }

        convertView.setOnClickListener(v -> {
            listener.onSearchItemClick(position, item);
        });
        Descriptor descriptor = item.getDescriptor();
        if (descriptor instanceof AppDescriptor || descriptor instanceof DeepShortcutDescriptor) {
            holder.info.setVisibility(View.VISIBLE);
        } else {
            holder.info.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    static class ViewHolder {
        final TextView text;
        final ImageView image;
        final ImageView info;
        int adapterPosition;

        ViewHolder(View itemView) {
            this.text = itemView.findViewById(R.id.text);
            this.image = itemView.findViewById(R.id.image);
            this.info = itemView.findViewById(R.id.info);
        }
    }

    public interface Listener {
        void onSearchItemClick(int position, Match match);

        void onSearchItemInfoClick(int position, Match match);
    }

    class SearchFilter extends Filter {
        private final SearchRepository searchRepository;

        SearchFilter(SearchRepository searchRepository) {
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

