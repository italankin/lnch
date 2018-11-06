package com.italankin.lnch.model.viewmodel;

/**
 * Item which can be hidden by user
 * <br>
 * This interface is needed to perform correct operations on list, such as moving
 */
public interface HiddenItem extends DescriptorItem {

    void setHidden(boolean hidden);

    boolean isHidden();
}
