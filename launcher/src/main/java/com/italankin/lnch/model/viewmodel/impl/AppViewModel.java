package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.viewmodel.BadgeItem;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.HiddenItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;

import java.util.Objects;

public final class AppViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem, HiddenItem,
        VisibleItem, BadgeItem {

    public final String componentName;
    public final String packageName;
    private final AppDescriptor descriptor;
    private final int color;
    private final String label;
    private boolean hidden;
    private boolean visible;
    private boolean searchVisible;
    private boolean shortcutsSearchVisible;
    private String customLabel;
    private Integer customColor;
    private boolean badgeVisible;

    public AppViewModel(AppDescriptor descriptor) {
        this.descriptor = descriptor;
        this.packageName = descriptor.packageName;
        this.componentName = descriptor.componentName;
        this.label = descriptor.label;
        this.hidden = descriptor.hidden;
        this.visible = !descriptor.hidden;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.searchVisible = descriptor.searchVisible;
        this.shortcutsSearchVisible = descriptor.shortcutsSearchVisible;
    }

    public AppViewModel(AppViewModel item) {
        this.descriptor = item.descriptor;
        this.packageName = item.packageName;
        this.componentName = item.componentName;
        this.label = item.label;
        this.hidden = item.hidden;
        this.visible = item.visible;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
        this.searchVisible = item.searchVisible;
        this.shortcutsSearchVisible = item.shortcutsSearchVisible;
        this.badgeVisible = item.badgeVisible;
    }

    @Override
    public AppDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
    }

    @Override
    public void setCustomLabel(String label) {
        customLabel = label;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public boolean isVisible() {
        return !hidden && visible;
    }

    public void setSearchVisible(boolean searchVisible) {
        this.searchVisible = searchVisible;
    }

    public boolean isSearchVisible() {
        return searchVisible;
    }

    public void setShortcutsSearchVisible(boolean shortcutsSearchVisible) {
        this.shortcutsSearchVisible = shortcutsSearchVisible;
    }

    public boolean isShortcutsSearchVisible() {
        return shortcutsSearchVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void setBadgeVisible(boolean visible) {
        badgeVisible = visible;
    }

    @Override
    public boolean isBadgeVisible() {
        return badgeVisible;
    }

    @Override
    public String toString() {
        return "App{" + packageName + (hidden ? "*" : "") + (badgeVisible ? "!" : "") + "}";
    }

    @Override
    public boolean is(DescriptorItem another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        AppViewModel that = (AppViewModel) another;
        return this.descriptor.equals(that.descriptor)
                && this.visible == that.visible
                && this.hidden == that.hidden;
    }

    @Override
    public boolean deepEquals(DescriptorItem another) {
        if (this.getClass() != another.getClass()) {
            return false;
        }
        AppViewModel that = (AppViewModel) another;
        return this.descriptor.equals(that.descriptor)
                && Objects.equals(this.customLabel, that.customLabel)
                && Objects.equals(this.customColor, that.customColor)
                && this.hidden == that.hidden
                && this.visible == that.visible
                && this.badgeVisible == that.badgeVisible;
    }
}
