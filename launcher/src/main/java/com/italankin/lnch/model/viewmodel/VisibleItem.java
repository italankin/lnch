package com.italankin.lnch.model.viewmodel;

/**
 * Item which can be added to group, termporarily hidden from user (group collapsing)
 */
public interface VisibleItem extends DescriptorItem {

    void setVisible(boolean visible);

    boolean isVisible();
}
