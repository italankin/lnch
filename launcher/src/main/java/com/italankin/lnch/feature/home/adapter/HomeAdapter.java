package com.italankin.lnch.feature.home.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;
import me.italankin.adapterdelegates.AdapterDelegate;
import me.italankin.adapterdelegates.CompositeAdapter;

@SuppressWarnings("rawtypes")
public final class HomeAdapter extends CompositeAdapter<DescriptorUi> {
    private UserPrefs.ItemPrefs itemPrefs;
    private ItemPrefsOverrides itemPrefsOverrides;

    private HomeAdapter(Context context, SparseArrayCompat<AdapterDelegate> delegates, boolean hasStableIds) {
        super(context, delegates, hasStableIds);
    }

    public void setUserPrefsOverrides(@Nullable ItemPrefsOverrides userPrefsOverrides) {
        this.itemPrefsOverrides = userPrefsOverrides;
        forceUpdateItemPrefs();
    }

    public void forceUpdateItemPrefs() {
        updateItemPrefs(itemPrefs);
        notifyDataSetChanged();
    }

    public boolean updateUserPrefs(UserPrefs userPrefs) {
        return updateItemPrefs(userPrefs.itemPrefs);
    }

    private boolean updateItemPrefs(UserPrefs.ItemPrefs itemPrefs) {
        boolean needsFullUpdate = isChanged(itemPrefs);
        this.itemPrefs = itemPrefs;
        UserPrefs.ItemPrefs newItemPrefs = itemPrefs;
        if (itemPrefs != null && itemPrefsOverrides != null) {
            newItemPrefs = itemPrefsOverrides.getItemPrefsOverrides(itemPrefs);
        }
        for (int i = 0, size = delegates.size(); i < size; i++) {
            HomeAdapterDelegate delegate = (HomeAdapterDelegate) delegates.valueAt(i);
            delegate.setItemPrefs(newItemPrefs);
        }
        return needsFullUpdate;
    }

    private boolean isChanged(UserPrefs.ItemPrefs another) {
        if (itemPrefs != null && another != null) {
            return !itemPrefs.equals(another);
        }
        return true;
    }

    public interface ItemPrefsOverrides {
        UserPrefs.ItemPrefs getItemPrefsOverrides(UserPrefs.ItemPrefs itemPrefs);
    }

    public static class Builder extends BaseBuilder<DescriptorUi, Builder, HomeAdapter> {
        public Builder(Context context) {
            super(context);
        }

        @NonNull
        @Override
        protected HomeAdapter createAdapter() {
            return new HomeAdapter(context, delegates, hasStableIds);
        }
    }
}
