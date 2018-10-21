package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.model.viewmodel.VisibleItem;
import com.italankin.lnch.util.widget.StubView;

public class HiddenAppViewModelAdapter
        extends HomeAdapterDelegate<HiddenAppViewModelAdapter.ViewHolder, VisibleItem> {

    @NonNull
    @Override
    public ViewHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(new StubView(inflater.getContext()));
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof VisibleItem && !((VisibleItem) item).isVisible();
    }

    @Override
    public long getItemId(int position, VisibleItem item) {
        return item.getDescriptor().getId().hashCode();
    }

    @Override
    protected int getLayoutRes() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        throw new UnsupportedOperationException();
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<VisibleItem> {
        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(VisibleItem item) {
            // empty
        }

        @Nullable
        @Override
        TextView getLabel() {
            return null;
        }
    }
}
