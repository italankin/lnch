package com.italankin.lnch.feature.widgets.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import me.italankin.adapterdelegates.AdapterDelegate;
import me.italankin.adapterdelegates.CompositeAdapter;

import java.util.List;

public abstract class AbstractWidgetAdapter<VH extends AbstractWidgetAdapter.ViewHolder>
        implements AdapterDelegate<VH, AppWidget> {

    protected CompositeAdapter<?> adapter;

    @Override
    public void onAttached(CompositeAdapter<AppWidget> adapter) {
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public final VH onCreate(LayoutInflater inflater, ViewGroup parent) {
        throw new UnsupportedOperationException();
    }

    public abstract VH onCreate(AppWidget item, ViewGroup parent);

    @Override
    public void onBind(VH holder, int position, AppWidget item, List<?> payloads) {
        onBind(holder, position, item);
    }

    @Override
    public void onRecycled(VH holder) {
    }

    @Override
    public boolean onFailedToRecycle(VH holder) {
        return false;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppWidget;
    }

    @Override
    public long getItemId(int position, AppWidget item) {
        return item.hashCode();
    }

    abstract static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // do not recycle view holders, because the same widget id might be bound to different widgets
            setIsRecyclable(false);
        }
    }
}
