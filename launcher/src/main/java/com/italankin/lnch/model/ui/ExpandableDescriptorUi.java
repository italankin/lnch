package com.italankin.lnch.model.ui;

/**
 * Item which can be expanded/collapsed, showing/hiding items 'inside'
 */
public interface ExpandableDescriptorUi extends DescriptorUi {

    void setExpanded(boolean expanded);

    boolean isExpanded();
}
