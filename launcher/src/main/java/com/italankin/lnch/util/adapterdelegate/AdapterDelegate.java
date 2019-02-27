package com.italankin.lnch.util.adapterdelegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Interface for adapter delegate.
 *
 * @param <VH> type of the {@link RecyclerView.ViewHolder} for this delegate
 * @param <T>  data type for this delegate
 */
public interface AdapterDelegate<VH extends RecyclerView.ViewHolder, T> {

    /**
     * Called when this delegate is attached to {@code adapter}.
     *
     * @param adapter adapter
     */
    void onAttached(CompositeAdapter<T> adapter);

    /**
     * Create {@link RecyclerView.ViewHolder}.
     *
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     */
    @NonNull
    VH onCreate(LayoutInflater inflater, ViewGroup parent);

    /**
     * Bind {@code holder} to the {@code item}.
     *
     * @param holder   view holder
     * @param position position of {@code item}
     * @param item     item
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    void onBind(VH holder, int position, T item);

    /**
     * Bind {@code holder} to the {@code item}.
     *
     * @param holder   view holder
     * @param position position of {@code item}
     * @param item     item
     * @param payloads optional payloads
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int, List)
     */
    void onBind(VH holder, int position, T item, List<Object> payloads);

    /**
     * Called when {@code holder} gets {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder) recycled}.
     *
     * @param holder view holder
     */
    void onRecycled(VH holder);

    /**
     * Called when {@code holder} {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder) failed to recycle}.
     *
     * @param holder view holder
     * @return true if the View should be recycled, false otherwise
     * @see RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)
     */
    boolean onFailedToRecycle(RecyclerView.ViewHolder holder);

    /**
     * Check if {@code item} at {@code position} can be managed by this delegate.
     *
     * @param position position of {@code item}
     * @param item     item
     * @return {@code true}, if {@code item} is type managed by this delegate, otherwise {@code false}
     */
    boolean isType(int position, Object item);

    /**
     * Get an unique item ID.
     *
     * @param position position of {@code item}
     * @param item     item
     * @return unique identifier of {@code item}
     * @see RecyclerView.Adapter#hasStableIds()
     */
    long getItemId(int position, T item);

}
