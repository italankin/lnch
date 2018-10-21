package com.italankin.lnch.model.viewmodel.util;

import android.support.v7.util.DiffUtil;

import com.italankin.lnch.model.viewmodel.DescriptorItem;

import java.util.List;

public class DescriptorItemDiffCallback extends DiffUtil.Callback {
    private final List<DescriptorItem> oldList;
    private final List<DescriptorItem> newList;

    public DescriptorItemDiffCallback(List<DescriptorItem> oldList, List<DescriptorItem> newList) {
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
        DescriptorItem oldItem = oldList.get(oldItemPosition);
        DescriptorItem newItem = newList.get(newItemPosition);
        return oldItem.is(newItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        DescriptorItem oldItem = oldList.get(oldItemPosition);
        DescriptorItem newItem = newList.get(newItemPosition);
        return oldItem.deepEquals(newItem);
    }
}
