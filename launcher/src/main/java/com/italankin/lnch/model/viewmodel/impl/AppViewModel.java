package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.GroupedItem;
import com.italankin.lnch.model.viewmodel.HiddenItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;

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
