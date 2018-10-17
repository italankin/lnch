package com.italankin.lnch.model.viewmodel;

/**
 * Item which can be expanded/collapsed to show/hide items 'inside'
 */
public interface ExpandableItem extends DescriptorItem {
    void setExpanded(boolean expanded);

    boolean isExpanded();
}
