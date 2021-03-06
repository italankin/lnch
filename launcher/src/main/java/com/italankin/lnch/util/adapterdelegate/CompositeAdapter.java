package com.italankin.lnch.util.adapterdelegate;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter}, which manages {@link AdapterDelegate}s.
 *
 * @param <T> data type for this adapter
 */
@SuppressWarnings("unchecked")
public class CompositeAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected final LayoutInflater inflater;
    protected final SparseArray<AdapterDelegate> delegates;
    protected final Context context;

    @NonNull
    protected List<T> dataset = new ArrayList<>();

    protected CompositeAdapter(Context context, SparseArray<AdapterDelegate> delegates, boolean hasStableIds) {
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
        this.delegates = delegates;
        setHasStableIds(hasStableIds);
        for (int i = 0, s = delegates.size(); i < s; i++) {
            delegates.valueAt(i).onAttached(this);
        }
    }

    /**
     * Set dataset for this adapter.
     *
     * @param data new dataset
     */
    public void setDataset(@Nullable List<T> data) {
        dataset = data != null ? data : new ArrayList<>();
    }

    /**
     * Get item at {@code position}.
     *
     * @param position position of item
     * @return item at {@code position}
     */
    public T getItem(int position) {
        return dataset.get(position);
    }

    /**
     * Get dataset, provided by {@link #setDataset(List)}.
     *
     * @return dateset
     */
    @NonNull
    public List<T> getDataset() {
        return dataset;
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getLayoutInflater() {
        return inflater;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegates.get(viewType).onCreate(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        getDelegate(position).onBind(holder, position, getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        getDelegate(position).onBind(holder, position, getItem(position), payloads);
    }

    @Override
    public int getItemViewType(int position) {
        T item = getItem(position);
        for (int i = 0, s = delegates.size(); i < s; i++) {
            if (delegates.valueAt(i).isType(position, item)) {
                return delegates.keyAt(i);
            }
        }
        throw new IllegalArgumentException("Cannot get type for item at pos=" + position + ", item=" + item.toString());
    }

    @Override
    public long getItemId(int position) {
        return getDelegate(position).getItemId(position, getItem(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        delegates.get(holder.getItemViewType()).onRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return delegates.get(holder.getItemViewType()).onFailedToRecycle(holder);
    }

    /**
     * Get delegate for item at {@code position}.
     *
     * @param position position of item
     * @return delegate for view at {@code position}
     * @throws IllegalArgumentException if adapter was not found
     */
    @NonNull
    protected AdapterDelegate getDelegate(int position) {
        int viewType = getItemViewType(position);
        AdapterDelegate delegate = delegates.get(viewType);
        if (delegate == null) {
            throw new IllegalArgumentException("No AdapterDelegate found for item at pos=" + position + ", viewType=" +
                    viewType);
        }
        return delegate;
    }

    public static class Builder<T> extends BaseBuilder<T, Builder<T>> {
        public Builder(Context context) {
            super(context);
        }
    }

    /**
     * Builder for {@link CompositeAdapter}.
     *
     * @param <T> list data type
     */
    protected abstract static class BaseBuilder<T, B> {
        protected final Context context;
        protected final SparseArray<AdapterDelegate> delegates = new SparseArray<>(1);
        protected boolean hasStableIds;
        protected List<T> dataset;
        protected RecyclerView recyclerView;

        protected BaseBuilder(Context context) {
            this.context = context;
        }

        /**
         * Whatever adapter should {@link CompositeAdapter#setHasStableIds(boolean) have stable ids}.
         *
         * @return this builder
         */
        public B setHasStableIds(boolean hasStableIds) {
            this.hasStableIds = hasStableIds;
            return (B) this;
        }

        /**
         * Add delegate for adapter to use.
         *
         * @param delegate delegate
         * @return this builder
         */
        public B add(AdapterDelegate<?, ? extends T> delegate) {
            return add(delegates.size(), delegate);
        }

        /**
         * Add delegate with explicit {@code viewType} for adapter to use.
         *
         * @param viewType view type; should be unique
         * @param delegate delegate
         * @return this builder
         */
        public B add(int viewType, AdapterDelegate<?, ? extends T> delegate) {
            if (delegate == null) {
                throw new NullPointerException("delegate == null");
            }
            if (delegates.indexOfKey(viewType) >= 0) {
                throw new IllegalArgumentException();
            }
            delegates.put(viewType, delegate);
            return (B) this;
        }

        /**
         * Provide dataset for adapter.
         *
         * @param dataset dataset
         * @return this builder
         */
        public B dataset(List<T> dataset) {
            this.dataset = dataset;
            return (B) this;
        }

        /**
         * Set adapter for the given {@code recyclerView}.
         *
         * @param recyclerView recycler view
         * @return this builder
         * @see RecyclerView#setAdapter(RecyclerView.Adapter)
         */
        public B recyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return (B) this;
        }

        /**
         * Create instance of {@link CompositeAdapter}, bind dataset, set adapter for {@link RecyclerView}.
         *
         * @return newly created instance of {@link CompositeAdapter}
         */
        public CompositeAdapter<T> create() {
            if (delegates.size() == 0) {
                throw new IllegalStateException("No AdapterDelegates added");
            }
            CompositeAdapter<T> adapter = createAdapter();
            if (dataset != null) {
                adapter.dataset = dataset;
            }
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }
            return adapter;
        }

        protected CompositeAdapter<T> createAdapter() {
            return new CompositeAdapter<>(context, delegates, hasStableIds);
        }
    }
}
