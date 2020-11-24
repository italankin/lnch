package com.italankin.lnch.model.ui;

/**
 * Item which can be ignored (hidden) by user
 * <br>
 * This interface is needed to perform correct operations on list, such as moving
 */
public interface IgnorableDescriptorUi extends DescriptorUi {

    void setIgnored(boolean ignored);

    boolean isIgnored();
}
