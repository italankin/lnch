package com.italankin.lnch.feature.home.model;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import com.italankin.lnch.model.viewmodel.DescriptorItem;

import java.util.Collections;
import java.util.List;

public final class Update {
    public static final Update EMPTY = new Update(Collections.emptyList(), null);

    public final List<DescriptorItem> items;
    public UserPrefs userPrefs;
    @Nullable
    private final DiffUtil.DiffResult diffResult;

    public Update(List<DescriptorItem> items, @Nullable DiffUtil.DiffResult diffResult) {
        this.items = items;
        this.diffResult = diffResult;
    }

    public Update with(UserPrefs userPrefs) {
        this.userPrefs = userPrefs;
        return this;
    }

    public void dispatchTo(RecyclerView.Adapter<?> adapter) {
        if (diffResult != null) {
            diffResult.dispatchUpdatesTo(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\nitems(" + items.size() + ")=" + items +
                "\nuserPrefs=" + userPrefs +
                "\n}";
    }
}
