package com.italankin.lnch.ui.feature.home;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.italankin.lnch.util.adapterdelegate.AdapterDelegate;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;

class HiddenAppViewModelAdapter implements AdapterDelegate<HiddenAppViewModelHolder, AppViewModel> {
    @Override
    public void onAttached(CompositeAdapter<AppViewModel> adapter) {
        // empty
    }

    @NonNull
    @Override
    public HiddenAppViewModelHolder onCreate(LayoutInflater inflater, ViewGroup parent) {
        return new HiddenAppViewModelHolder(new View(inflater.getContext()));
    }

    @Override
    public void onBind(HiddenAppViewModelHolder holder, int position, AppViewModel item) {
        // empty
    }

    @Override
    public void onRecycled(HiddenAppViewModelHolder holder) {
        // empty
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppViewModel && ((AppViewModel) item).hidden;
    }

    @Override
    public long getItemId(int position, AppViewModel item) {
        return item.hashCode();
    }
}

class HiddenAppViewModelHolder extends RecyclerView.ViewHolder {
    public HiddenAppViewModelHolder(View itemView) {
        super(itemView);
    }
}
