package com.italankin.lnch.model.ui.impl;

import com.italankin.lnch.model.descriptor.impl.DividerDescriptor;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;

import java.util.Objects;

public final class DividerDescriptorUi implements CustomLabelDescriptorUi, CustomColorDescriptorUi, RemovableDescriptorUi {
    private final DividerDescriptor descriptor;
    private String customLabel;
    private Integer customColor;

    public DividerDescriptorUi(DividerDescriptor descriptor) {
        this.descriptor = descriptor;
        customLabel = descriptor.customLabel;
        customColor = descriptor.customColor;
    }

    @Override
    public DividerDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getLabel() {
        return descriptor.getLabel();
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
        return descriptor.color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public boolean is(DescriptorUi other) {
        return other instanceof DividerDescriptorUi && descriptor.equals(other.getDescriptor());
    }

    @Override
    public boolean deepEquals(DescriptorUi other) {
        return is(other)
                && getColor() == ((DividerDescriptorUi) other).getColor()
                && Objects.equals(customLabel, ((DividerDescriptorUi) other).customLabel)
                && Objects.equals(customColor, ((DividerDescriptorUi) other).customColor);
    }
}
