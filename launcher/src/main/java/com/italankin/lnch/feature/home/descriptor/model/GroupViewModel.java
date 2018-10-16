package com.italankin.lnch.feature.home.descriptor.model;

import com.italankin.lnch.feature.home.descriptor.CustomColorItem;
import com.italankin.lnch.feature.home.descriptor.CustomLabelItem;
import com.italankin.lnch.feature.home.descriptor.DescriptorItem;
import com.italankin.lnch.feature.home.descriptor.ExpandableItem;
import com.italankin.lnch.feature.home.descriptor.RemovableItem;
import com.italankin.lnch.model.repository.descriptors.model.GroupDescriptor;

public class GroupViewModel implements DescriptorItem, CustomColorItem, CustomLabelItem,
        RemovableItem, ExpandableItem {
    private final GroupDescriptor descriptor;
    private final String label;
    private String customLabel;
    private boolean expanded = true;
    private int color;
    private Integer customColor;

    public GroupViewModel(GroupDescriptor descriptor) {
        this.descriptor = descriptor;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public GroupDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getVisibleLabel() {
        return customLabel != null ? customLabel : label;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
    }

    @Override
    public void setCustomLabel(String label) {
        this.customLabel = label;
    }

    @Override
    public int getVisibleColor() {
        return customColor != null ? customColor : color;
    }

    @Override
    public void setCustomColor(Integer color) {
        this.customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }
}
