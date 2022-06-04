package com.italankin.lnch.model.ui.impl;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.ui.BadgeDescriptorUi;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;

import java.util.Objects;

import androidx.annotation.NonNull;

public final class AppDescriptorUi implements DescriptorUi,
        CustomLabelDescriptorUi,
        CustomColorDescriptorUi,
        IgnorableDescriptorUi,
        InFolderDescriptorUi,
        BadgeDescriptorUi {

    public static final Object PAYLOAD_BADGE = new Object();

    public final String componentName;
    public final String packageName;
    private final AppDescriptor descriptor;
    private final int color;
    private final String label;
    private boolean ignored;
    private String customLabel;
    private Integer customColor;
    private final Integer customBadgeColor;
    private boolean badgeVisible;

    public AppDescriptorUi(AppDescriptor descriptor) {
        this.descriptor = descriptor;
        this.packageName = descriptor.packageName;
        this.componentName = descriptor.componentName;
        this.label = descriptor.label;
        this.ignored = descriptor.ignored;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.customBadgeColor = descriptor.customBadgeColor;
    }

    public AppDescriptorUi(AppDescriptorUi item) {
        this.descriptor = item.descriptor;
        this.packageName = item.packageName;
        this.componentName = item.componentName;
        this.label = item.label;
        this.ignored = item.ignored;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
        this.customBadgeColor = item.customBadgeColor;
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
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
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
    public Integer getCustomBadgeColor() {
        return customBadgeColor;
    }

    @NonNull
    @Override
    public String toString() {
        return "App{" + packageName + (ignored ? "*" : "") + (badgeVisible ? "!" : "") + "}";
    }

    @Override
    public boolean is(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        AppDescriptorUi that = (AppDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && this.ignored == that.ignored;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        if (this.getClass() != another.getClass()) {
            return false;
        }
        AppDescriptorUi that = (AppDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && Objects.equals(this.customLabel, that.customLabel)
                && Objects.equals(this.customColor, that.customColor)
                && this.ignored == that.ignored
                && this.badgeVisible == that.badgeVisible;
    }

    @Override
    public Object getChangePayload(DescriptorUi oldItem) {
        if (this.badgeVisible != ((AppDescriptorUi) oldItem).badgeVisible) {
            return PAYLOAD_BADGE;
        }
        return null;
    }
}
