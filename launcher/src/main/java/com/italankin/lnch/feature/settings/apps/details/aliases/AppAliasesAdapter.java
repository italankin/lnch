package com.italankin.lnch.feature.settings.apps.details.aliases;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.R;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class AppAliasesAdapter extends RecyclerView.Adapter<AppAliasesAdapter.Holder> {

    private final Listener listener;
    private List<String> dataset = Collections.emptyList();

    AppAliasesAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    void setDataset(List<String> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppAliasesAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings_app_alias, parent, false);
        Holder holder = new Holder(view);
        holder.delete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(pos);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppAliasesAdapter.Holder holder, int position) {
        holder.label.setText(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public long getItemId(int position) {
        return dataset.get(position).hashCode();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView label;
        final View delete;

        Holder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    interface Listener {
        void onDeleteClick(int position);
    }
}
