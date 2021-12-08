package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FolderDescriptorUiAdapter
        extends HomeAdapterDelegate<FolderDescriptorUiAdapter.ViewHolder, FolderDescriptorUi> {

    private final Listener listener;

    public FolderDescriptorUiAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_folder;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFolderClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFolderLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof FolderDescriptorUi;
    }

    public interface Listener {
        void onFolderClick(int position, FolderDescriptorUi item);

        void onFolderLongClick(int position, FolderDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<FolderDescriptorUi> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        void bind(FolderDescriptorUi item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
        }

        @Nullable
        @Override
        TextView getLabel() {
            return label;
        }
    }
}
