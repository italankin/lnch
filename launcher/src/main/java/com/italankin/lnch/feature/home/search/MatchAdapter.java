package com.italankin.lnch.feature.home.search;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;
import com.squareup.picasso.Picasso;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class MatchAdapter extends BaseAdapterDelegate<MatchAdapter.ViewHolder, Match> {

    private final Picasso picasso;
    private final Listener listener;

    MatchAdapter(Picasso picasso, Listener listener) {
        this.picasso = picasso;
        this.listener = listener;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof Match;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_search_match;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onSearchItemClick(position, getItem(position));
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Match item = getItem(position);
                Set<Match.Action> actions = item.availableActions();
                if (actions.contains(Match.Action.INFO)) {
                    listener.onSearchItemInfoClick(position, item);
                    return true;
                }
            }
            return false;
        });
        holder.pin.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onSearchItemPinClick(position, getItem(position));
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, Match item) {
        Context context = holder.itemView.getContext();
        holder.text.setText(item.getLabel(context));
        holder.text.setTextColor(item.getColor(context));

        Uri icon = item.getIcon();
        if (icon != null) {
            picasso.load(icon)
                    .centerInside()
                    .resizeDimen(R.dimen.search_match_icon_size, R.dimen.search_match_icon_size)
                    .into(holder.image);
        } else {
            picasso.cancelRequest(holder.image);
            holder.image.setImageResource(item.getIconResource());
        }

        Set<Match.Action> actions = item.availableActions();
        if (actions.contains(Match.Action.PIN)) {
            holder.pin.setVisibility(View.VISIBLE);
        } else {
            holder.pin.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position, Match item) {
        return item.hashCode();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ImageView image;
        final ImageView pin;

        ViewHolder(View itemView) {
            super(itemView);
            this.text = itemView.findViewById(R.id.text);
            this.image = itemView.findViewById(R.id.image);
            this.pin = itemView.findViewById(R.id.pin);
        }
    }

    public interface Listener {
        void onSearchItemClick(int position, Match match);

        void onSearchItemPinClick(int position, Match match);

        void onSearchItemInfoClick(int position, Match match);
    }
}

