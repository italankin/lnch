package com.italankin.lnch.feature.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.util.widget.StubView;

public class IgnorableDescriptorUiAdapter
        extends HomeAdapterDelegate<IgnorableDescriptorUiAdapter.ViewHolder, IgnorableDescriptorUi> {

    private static final int IGNORED_FLAG = 1 << 22;

    public IgnorableDescriptorUiAdapter() {
        super(new Params(true, true));
    }

    @NonNull
    @Override
    public ViewHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(new StubView(inflater.getContext()));
    }

    @Override
    public long getItemId(int position, IgnorableDescriptorUi item) {
        return super.getItemId(position, item) ^ IGNORED_FLAG;
    }

    @Override
    public boolean isType(int position, Object item, boolean ignoreVisibility) {
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

        @Override
        protected View getRoot() {
            return null;
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return null;
        }
    }
}
