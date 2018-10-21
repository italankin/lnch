package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;

public class AppViewModelAdapter
        extends HomeAdapterDelegate<AppViewModelAdapter.ViewHolder, AppViewModel> {

    private final Listener listener;

    public AppViewModelAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppLongClick(pos, getItem(pos));
                return true;
            }
            return false;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppViewModel && ((AppViewModel) item).isVisible();
    }

    public interface Listener {
        void onAppClick(int position, AppViewModel item);

        void onAppLongClick(int position, AppViewModel item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<AppViewModel> {
        final TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
        }

        @Override
        void bind(AppViewModel item) {
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
