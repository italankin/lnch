package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.AppItem;

import java.util.Collections;
import java.util.List;

class AppItemAdapter extends RecyclerView.Adapter<AppItemViewHolder> {
    private final LayoutInflater inflater;
    private List<AppItem> dataset;
    private final Listener listener;

    AppItemAdapter(Context context, Listener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setDataset(List<AppItem> newDataset) {
        dataset = newDataset != null ? newDataset : Collections.emptyList();
        notifyDataSetChanged();
    }

    @Override
    public AppItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_app, parent, false);
        AppItemViewHolder holder = new AppItemViewHolder(view);
        view.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pos, dataset.get(pos));
                }
            }
        });
        view.setOnLongClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(pos, dataset.get(pos));
                    return true;
                }
            }
            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(AppItemViewHolder holder, int position) {
        AppItem item = dataset.get(position);
        holder.label.setText(item.getLabel());
        holder.label.setTextColor(item.getColor());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public long getItemId(int position) {
        return dataset.get(position).hashCode();
    }

    public interface Listener {
        void onItemClick(int position, AppItem item);

        void onItemLongClick(int position, AppItem item);
    }
}

class AppItemViewHolder extends RecyclerView.ViewHolder {
    final TextView label;

    AppItemViewHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }
}