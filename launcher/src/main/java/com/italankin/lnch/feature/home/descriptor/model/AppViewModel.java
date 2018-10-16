package com.italankin.lnch.feature.home.descriptor.model;

import com.italankin.lnch.feature.home.descriptor.CustomColorItem;
import com.italankin.lnch.feature.home.descriptor.CustomLabelItem;
import com.italankin.lnch.feature.home.descriptor.DescriptorItem;
import com.italankin.lnch.feature.home.descriptor.GroupedItem;
import com.italankin.lnch.feature.home.descriptor.HiddenItem;
import com.italankin.lnch.feature.home.descriptor.VisibleItem;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;

public class AppViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem, HiddenItem,
        VisibleItem, GroupedItem {
    public final String componentName;
    public final String packageName;
    private final AppDescriptor descriptor;
    private final int color;
    private final String label;
    private boolean hidden;
    private boolean visible;
    private String customLabel;
    private Integer customColor;

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
    }

    @Override
    public AppDescriptor getDescriptor() {
        return descriptor;
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
    public String getVisibleLabel() {
        return customLabel != null ? customLabel : label;
    }

    @Override
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    public int getVisibleColor() {
        return customColor != null ? customColor : color;
    }

    @Override
    public boolean isVisible() {
        return !hidden && visible;
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
    public String toString() {
        return descriptor.toString();
    }
}
