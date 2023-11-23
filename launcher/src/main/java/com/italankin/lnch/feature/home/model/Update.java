package com.italankin.lnch.feature.home.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Collections;
import java.util.List;

public final class Update {
    public static final Update EMPTY = new Update(Collections.emptyList(), null);

    public final List<DescriptorUi> items;
    public UserPrefs userPrefs;
    @Nullable
    private DiffUtil.DiffResult diffResult;

    public Update(List<DescriptorUi> items, @Nullable DiffUtil.DiffResult diffResult) {
        this.items = items;
        this.diffResult = diffResult;
    }

    @NonNull
    public Update with(UserPrefs userPrefs) {
        this.userPrefs = userPrefs;
        return this;
    }

    public void dispatchTo(RecyclerView.Adapter<?> adapter) {
        if (diffResult != null) {
            diffResult.dispatchUpdatesTo(adapter);
            diffResult = null;
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "\nitems(" + items.size() + ")=" + items +
                "\nuserPrefs=" + userPrefs +
                "\n}";
    }
}
