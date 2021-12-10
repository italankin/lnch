package com.italankin.lnch.feature.home.apps.folder.empty;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.adapter.HomeAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EmptyFolderDescriptorUiAdapter extends
        HomeAdapterDelegate<EmptyFolderDescriptorUiAdapter.ViewHolder, EmptyFolderDescriptorUi> {

    @Override
    protected int getLayoutRes() {
        return R.layout.item_folder_empty;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof EmptyFolderDescriptorUi;
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<EmptyFolderDescriptorUi> {

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(EmptyFolderDescriptorUi item) {
            // empty
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return null;
        }
    }
}
