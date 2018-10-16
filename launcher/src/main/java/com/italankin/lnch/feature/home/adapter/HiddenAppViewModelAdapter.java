package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.italankin.lnch.feature.home.descriptor.VisibleItem;
import com.italankin.lnch.util.adapterdelegate.AdapterDelegate;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.StubView;

public class HiddenAppViewModelAdapter implements AdapterDelegate<HiddenAppViewModelHolder, VisibleItem> {
    @Override
    public void onAttached(CompositeAdapter<VisibleItem> adapter) {
        // empty
    }

    @NonNull
    @Override
    public HiddenAppViewModelHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new HiddenAppViewModelHolder(new StubView(inflater.getContext()));
    }

    @Override
    public void onBind(HiddenAppViewModelHolder holder, int position, VisibleItem item) {
        // empty
    }

    @Override
    public void onRecycled(HiddenAppViewModelHolder holder) {
        // empty
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof VisibleItem && !((VisibleItem) item).isVisible();
    }

    @Override
    public long getItemId(int position, VisibleItem item) {
        return item.hashCode();
    }
}

class HiddenAppViewModelHolder extends RecyclerView.ViewHolder {
    HiddenAppViewModelHolder(View itemView) {
        super(itemView);
    }
}
