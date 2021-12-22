package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.model.ui.DescriptorUi;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public final class Update {
    public static final Update EMPTY = new Update(Collections.emptyList(), null);

    public final List<DescriptorUi> items;
    public UserPrefs userPrefs;
    @Nullable
    private final DiffUtil.DiffResult diffResult;

    public Update(List<DescriptorUi> items, @Nullable DiffUtil.DiffResult diffResult) {
        this.items = items;
        this.diffResult = diffResult;
    }

    @NotNull
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

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "\nitems(" + items.size() + ")=" + items +
                "\nuserPrefs=" + userPrefs +
                "\n}";
    }
}
