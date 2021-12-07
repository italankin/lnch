package com.italankin.lnch.feature.widgets.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.util.adapterdelegate.AdapterDelegate;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractWidgetAdapter<VH extends AbstractWidgetAdapter.ViewHolder>
        implements AdapterDelegate<VH, AppWidget> {

    @Override
    public void onAttached(CompositeAdapter<AppWidget> adapter) {
    }

    @NonNull
    @Override
    public final VH onCreate(LayoutInflater inflater, ViewGroup parent) {
        throw new UnsupportedOperationException();
    }

    public abstract VH onCreate(AppWidget item);

    @Override
    public void onBind(VH holder, int position, AppWidget item, List<Object> payloads) {
        onBind(holder, position, item);
    }

    @Override
    public void onRecycled(VH holder) {
    }

    @Override
    public boolean onFailedToRecycle(RecyclerView.ViewHolder holder) {
        return false;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppWidget;
    }

    @Override
    public long getItemId(int position, AppWidget item) {
        return item.appWidgetId;
    }

    abstract static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // do not recycle view holders, because the same widget id might be bound to different widgets
            setIsRecyclable(false);
        }
    }
}
