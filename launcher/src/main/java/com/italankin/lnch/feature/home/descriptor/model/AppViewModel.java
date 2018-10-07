package com.italankin.lnch.feature.home.descriptor.model;

import com.italankin.lnch.feature.home.descriptor.CustomColorItem;
import com.italankin.lnch.feature.home.descriptor.CustomLabelItem;
import com.italankin.lnch.feature.home.descriptor.DescriptorItem;
import com.italankin.lnch.feature.home.descriptor.GroupedItem;
import com.italankin.lnch.feature.home.descriptor.HiddenItem;
import com.italankin.lnch.feature.home.descriptor.VisibleItem;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;

public class AppViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem, HiddenItem,
        VisibleItem, GroupedItem {
    public final AppDescriptor item;
    public final String packageName;
    public final String componentName;
    public final int color;
    public final String label;
    public boolean hidden;
    public boolean visible;
    public String customLabel;
    public Integer customColor;

    public AppViewModel(AppDescriptor item) {
        this.item = item;
        this.packageName = item.packageName;
        this.componentName = item.componentName;
        this.label = item.label;
        this.hidden = item.hidden;
        this.visible = !item.hidden;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
    }

    @Override
    public Descriptor getDescriptor() {
        return item;
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
        return item.toString();
    }
}
