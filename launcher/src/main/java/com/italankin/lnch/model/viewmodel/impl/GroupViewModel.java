package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.ExpandableItem;
import com.italankin.lnch.model.viewmodel.RemovableItem;

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
    public int getColor() {
        return color;
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
        this.customLabel = label;
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
