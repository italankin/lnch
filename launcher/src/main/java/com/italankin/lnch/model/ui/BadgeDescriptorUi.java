package com.italankin.lnch.model.ui;

/**
 * An item which can have a badge on it
 */
public interface BadgeDescriptorUi extends DescriptorUi {

    void setBadgeVisible(boolean visible);

    boolean isBadgeVisible();
}
