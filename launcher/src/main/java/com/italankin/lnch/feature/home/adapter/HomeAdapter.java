package com.italankin.lnch.feature.home.adapter;

import android.content.Context;
import android.util.SparseArray;

import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.adapterdelegate.AdapterDelegate;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;

@SuppressWarnings("rawtypes")
public final class HomeAdapter extends CompositeAdapter<DescriptorUi> {
    private UserPrefs.ItemPrefs itemPrefs;

    private HomeAdapter(Context context, SparseArray<AdapterDelegate> delegates, boolean hasStableIds) {
        super(context, delegates, hasStableIds);
    }

    public boolean updateUserPrefs(UserPrefs userPrefs) {
        boolean needsFullUpdate = isChanged(userPrefs);
        this.itemPrefs = userPrefs.itemPrefs;
        for (int i = 0, size = delegates.size(); i < size; i++) {
            HomeAdapterDelegate delegate = (HomeAdapterDelegate) delegates.valueAt(i);
            delegate.setItemPrefs(userPrefs.itemPrefs);
        }
        return needsFullUpdate;
    }

    private boolean isChanged(UserPrefs another) {
        if (itemPrefs != null && another != null) {
            return !itemPrefs.equals(another.itemPrefs);
        }
        return true;
    }

    public static class Builder extends BaseBuilder<DescriptorUi, Builder> {
        public Builder(Context context) {
            super(context);
        }

        @Override
        public HomeAdapter create() {
            return (HomeAdapter) super.create();
        }

        @Override
        protected CompositeAdapter<DescriptorUi> createAdapter() {
            return new HomeAdapter(context, delegates, hasStableIds);
        }
    }
}
