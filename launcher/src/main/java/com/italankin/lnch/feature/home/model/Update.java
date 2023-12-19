package com.italankin.lnch.feature.home.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.feature.home.adapter.shimmer.ShimmerDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Arrays;
import java.util.List;

public final class Update {
    public static final Update INITIAL = new Update(Arrays.asList(
            new ShimmerDescriptorUi(96),
            new ShimmerDescriptorUi(120),
            new ShimmerDescriptorUi(109),
            new ShimmerDescriptorUi(199),
            new ShimmerDescriptorUi(140),
            new ShimmerDescriptorUi(280),
            new ShimmerDescriptorUi(92),
            new ShimmerDescriptorUi(201)
    ), true);

    public final List<DescriptorUi> items;
    private final boolean isTransient;
    public UserPrefs userPrefs;
    @Nullable
    private DiffUtil.DiffResult diffResult;

    public Update(List<DescriptorUi> items, @Nullable DiffUtil.DiffResult diffResult) {
        this(items, diffResult, false);
    }

    public Update(List<DescriptorUi> items, boolean isTransient) {
        this(items, null, isTransient);
    }

    public Update(List<DescriptorUi> items, @Nullable DiffUtil.DiffResult diffResult, boolean isTransient) {
        this.items = items;
        this.diffResult = diffResult;
        this.isTransient = isTransient;
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

    /**
     * Indicates transient state of this update, e.g. shimmers, error
     */
    public boolean isTransient() {
        return isTransient;
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
