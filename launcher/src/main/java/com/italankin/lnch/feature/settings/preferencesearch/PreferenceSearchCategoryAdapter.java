package com.italankin.lnch.feature.settings.preferencesearch;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import me.italankin.adapterdelegates.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class PreferenceSearchCategoryAdapter extends
        BaseAdapterDelegate<PreferenceSearchCategoryAdapter.ViewHolder, PreferenceSearchCategory> {

    @Override
    protected int getLayoutRes() {
        return R.layout.item_settings_preference_search_category;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBind(ViewHolder holder, int position, PreferenceSearchCategory item) {
        holder.bind(position, item);
    }

    @Override
    public long getItemId(int position, PreferenceSearchCategory item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof PreferenceSearchCategory;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }

        void bind(int position, PreferenceSearchCategory item) {
            title.setText(item.category);
            if (position == 0) {
                title.setBackground(null);
            } else {
                title.setBackgroundResource(R.drawable.settings_category_bg);
            }
        }
    }
}
