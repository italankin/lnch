package com.italankin.lnch.model.viewmodel;

/**
 * Item which can be hidden by user
 */
public interface HiddenItem extends DescriptorItem {

    void setHidden(boolean hidden);

    boolean isHidden();
}
