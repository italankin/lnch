package com.italankin.lnch.feature.intentfactory.extras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class IntentExtrasAdapter extends RecyclerView.Adapter<IntentExtrasAdapter.ViewHolder> {

    private final List<IntentExtra> dataset;
    private final Listener listener;

    IntentExtrasAdapter(List<IntentExtra> dataset, Listener listener) {
        this.dataset = dataset;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intent_extra, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position, dataset.get(position));
            }
        });
        holder.viewDelete.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IntentExtra item = dataset.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textKey;
        final TextView textValue;
        final TextView textType;
        final View viewDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textKey = itemView.findViewById(R.id.key);
            textValue = itemView.findViewById(R.id.value);
            textType = itemView.findViewById(R.id.type);
            viewDelete = itemView.findViewById(R.id.delete);
        }

        public void bind(IntentExtra item) {
            textKey.setText(item.key);
            textValue.setText(String.valueOf(item.value));
            textType.setText(item.type.name);
        }
    }

    interface Listener {
        void onItemClick(int position, IntentExtra item);

        void onDeleteClick(int position);
    }
}
