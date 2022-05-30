package com.italankin.lnch.feature.home.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

import java.util.List;

public abstract class HomeAdapterDelegate<VH extends HomeAdapterDelegate.ViewHolder<T>, T extends DescriptorUi>
        extends BaseAdapterDelegate<VH, T> {

    private final Params params;
    private UserPrefs.ItemPrefs itemPrefs;

    protected HomeAdapterDelegate() {
        this(Params.DEFAULT);
    }

    protected HomeAdapterDelegate(Params params) {
        this.params = params;
    }

    @Override
    public final void onBind(VH holder, int position, T item) {
        updateHolderView(holder);
        holder.bind(item);
    }

    @Override
    public final void onBind(VH holder, int position, T item, List<Object> payloads) {
        updateHolderView(holder);
        holder.bind(item, payloads);
    }

    @NonNull
    @Override
    public VH onCreate(LayoutInflater inflater, ViewGroup parent) {
        VH holder = super.onCreate(inflater, parent);
        updateHolderView(holder);
        return holder;
    }

    @Override
    public long getItemId(int position, T item) {
        return item.getDescriptor().getId().hashCode();
    }

    @Override
    public final boolean isType(int position, Object item) {
        return isType(position, item, params.ignoreVisibility);
    }

    protected abstract boolean isType(int position, Object item, boolean ignoreVisibility);

    void setItemPrefs(UserPrefs.ItemPrefs itemPrefs) {
        this.itemPrefs = itemPrefs;
    }

    private void updateHolderView(VH holder) {
        try {
            TextView label = holder.getLabel();
            if (label == null || itemPrefs == null || itemPrefs.equals(holder.itemPrefs)) {
                return;
            }
            update(holder, label, itemPrefs);
        } finally {
            holder.itemPrefs = itemPrefs;
        }
    }

    protected void update(VH holder, TextView label, UserPrefs.ItemPrefs itemPrefs) {
        ViewUtils.setPaddingDp(label, itemPrefs.itemPadding);
        label.setTextSize(itemPrefs.itemTextSize);
        int shadowColor = itemPrefs.itemShadowColor != null
                ? itemPrefs.itemShadowColor
                : ResUtils.resolveColor(label.getContext(), R.attr.colorItemShadowDefault);
        label.setShadowLayer(itemPrefs.itemShadowRadius, label.getShadowDx(),
                label.getShadowDy(), shadowColor);
        label.setTypeface(itemPrefs.itemFont.typeface());
        ViewGroup.LayoutParams lp = label.getLayoutParams();
        if (!params.ignoreAlignment && itemPrefs.matchParent) {
            if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                label.requestLayout();
            }
            switch (itemPrefs.alignment) {
                case START:
                    label.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                    break;
                case CENTER:
                    label.setGravity(Gravity.CENTER);
                    break;
                case END:
                    label.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                    break;
            }
        } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            label.requestLayout();
        }
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        UserPrefs.ItemPrefs itemPrefs;

        protected ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(T item);

        protected void bind(T item, List<Object> payloads) {
            bind(item);
        }

        @Nullable
        protected abstract TextView getLabel();
    }

    public static class Params {

        public static final Params DEFAULT = new Params(false, false);

        final boolean ignoreVisibility;
        final boolean ignoreAlignment;

        public Params(boolean ignoreVisibility, boolean ignoreAlignment) {
            this.ignoreVisibility = ignoreVisibility;
            this.ignoreAlignment = ignoreAlignment;
        }
    }
}
