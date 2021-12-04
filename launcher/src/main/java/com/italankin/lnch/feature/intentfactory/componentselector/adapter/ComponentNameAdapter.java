package com.italankin.lnch.feature.intentfactory.componentselector.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ComponentNameAdapter extends RecyclerView.Adapter<ComponentNameAdapter.ViewHolder> {

    private final Picasso picasso;
    private final Listener listener;
    private final List<ComponentNameUi> dataset = new ArrayList<>();

    public ComponentNameAdapter(Picasso picasso, Listener listener) {
        this.picasso = picasso;
        this.listener = listener;
    }

    public void setDataset(List<ComponentNameUi> dataset) {
        this.dataset.clear();
        this.dataset.addAll(dataset);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intent_component, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(dataset.get(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textPackage;
        final TextView textClass;
        final ImageView imageIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textPackage = itemView.findViewById(R.id.package_name);
            textClass = itemView.findViewById(R.id.class_name);
            imageIcon = itemView.findViewById(R.id.icon);
        }

        void bind(ComponentNameUi componentName) {
            textPackage.setText(componentName.packageName);
            textClass.setText(componentName.className);
            picasso.load(componentName.iconUri)
                    .fit()
                    .into(imageIcon);
        }
    }

    public interface Listener {
        void onItemClick(ComponentNameUi componentName);
    }
}
