package com.italankin.lnch.feature.settings.preferencesearch;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import me.italankin.adapterdelegates.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class PreferenceSearchAdapter extends BaseAdapterDelegate<PreferenceSearchAdapter.ViewHolder, PreferenceSearch> {

    private final Listener listener;

    PreferenceSearchAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_preference_search;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onPreferenceClick(getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public void onBind(ViewHolder holder, int position, PreferenceSearch item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, PreferenceSearch item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof PreferenceSearch;
    }

    public interface Listener {
        void onPreferenceClick(PreferenceSearch item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView summary;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
        }

        void bind(PreferenceSearch item) {
            title.setText(item.title);
            if (item.summary != 0) {
                summary.setText(item.summary);
                summary.setVisibility(View.VISIBLE);
            } else {
                summary.setText(null);
                summary.setVisibility(View.GONE);
            }
        }
    }
}
