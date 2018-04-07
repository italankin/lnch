package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.PackageModel;

import java.util.Collections;
import java.util.List;

class PackageModelAdapter extends RecyclerView.Adapter<PackageModelViewHolder> {
    private final LayoutInflater inflater;
    private final List<PackageModel> dataset;
    private final Listener listener;

    PackageModelAdapter(Context context, List<PackageModel> dataset, Listener listener) {
        this.inflater = LayoutInflater.from(context);
        this.dataset = dataset != null ? dataset : Collections.emptyList();
        this.listener = listener;
    }

    @Override
    public PackageModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_package, parent, false);
        PackageModelViewHolder holder = new PackageModelViewHolder(view);
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
    public void onBindViewHolder(PackageModelViewHolder holder, int position) {
        PackageModel item = dataset.get(position);
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
        void onItemClick(int position, PackageModel item);

        void onItemLongClick(int position, PackageModel item);
    }
}

class PackageModelViewHolder extends RecyclerView.ViewHolder {
    final TextView label;

    PackageModelViewHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }
}