package com.italankin.lnch.feature.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.util.widget.StubView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IgnorableDescriptorUiAdapter
        extends HomeAdapterDelegate<IgnorableDescriptorUiAdapter.ViewHolder, IgnorableDescriptorUi> {

    @NonNull
    @Override
    public ViewHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(new StubView(inflater.getContext()));
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof IgnorableDescriptorUi && ((IgnorableDescriptorUi) item).isIgnored();
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

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<IgnorableDescriptorUi> {
        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(IgnorableDescriptorUi item) {
            // empty
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return null;
        }
    }
}
