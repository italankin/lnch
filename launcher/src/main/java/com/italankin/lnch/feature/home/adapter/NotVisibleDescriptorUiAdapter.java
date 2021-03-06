package com.italankin.lnch.feature.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.model.ui.VisibleDescriptorUi;
import com.italankin.lnch.util.widget.StubView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotVisibleDescriptorUiAdapter
        extends HomeAdapterDelegate<NotVisibleDescriptorUiAdapter.ViewHolder, VisibleDescriptorUi> {

    @NonNull
    @Override
    public ViewHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(new StubView(inflater.getContext()));
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof VisibleDescriptorUi && !((VisibleDescriptorUi) item).isVisible();
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

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<VisibleDescriptorUi> {
        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(VisibleDescriptorUi item) {
            // empty
        }

        @Nullable
        @Override
        TextView getLabel() {
            return null;
        }
    }
}
