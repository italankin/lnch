package com.italankin.lnch.model.ui;

/**
 * Item which can be added to group and hidden/shown inside that
 */
public interface VisibleDescriptorUi extends DescriptorUi {

    void setVisible(boolean visible);

    boolean isVisible();
}
