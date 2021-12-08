package com.italankin.lnch.model.ui.impl;

import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.ExpandableDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;

import java.util.Objects;

public final class FolderDescriptorUi implements DescriptorUi,
        CustomColorDescriptorUi,
        CustomLabelDescriptorUi,
        RemovableDescriptorUi,
        ExpandableDescriptorUi {

    private final FolderDescriptor descriptor;
    private final String label;
    private final int color;
    private String customLabel;
    private Integer customColor;
    private boolean expanded = true;

    public FolderDescriptorUi(FolderDescriptor descriptor) {
        this.descriptor = descriptor;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public FolderDescriptor getDescriptor() {
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

    @Override
    public String toString() {
        return descriptor.toString();
    }

    @Override
    public boolean is(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        FolderDescriptorUi that = (FolderDescriptorUi) another;
        return this.descriptor.equals(that.descriptor);
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        FolderDescriptorUi that = (FolderDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && Objects.equals(this.customLabel, that.customLabel)
                && Objects.equals(this.customColor, that.customColor)
                && this.expanded == that.expanded;
    }
}
