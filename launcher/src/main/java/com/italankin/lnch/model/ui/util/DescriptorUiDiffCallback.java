package com.italankin.lnch.model.ui.util;

import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class DescriptorUiDiffCallback extends DiffUtil.Callback {
    private final List<DescriptorUi> oldList;
    private final List<DescriptorUi> newList;

    public DescriptorUiDiffCallback(List<DescriptorUi> oldList, List<DescriptorUi> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        DescriptorUi oldItem = oldList.get(oldItemPosition);
        DescriptorUi newItem = newList.get(newItemPosition);
        return oldItem.is(newItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        DescriptorUi oldItem = oldList.get(oldItemPosition);
        DescriptorUi newItem = newList.get(newItemPosition);
        return oldItem.deepEquals(newItem);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        DescriptorUi oldItem = oldList.get(oldItemPosition);
        DescriptorUi newItem = newList.get(newItemPosition);
        return newItem.getChangePayload(oldItem);
    }
}
