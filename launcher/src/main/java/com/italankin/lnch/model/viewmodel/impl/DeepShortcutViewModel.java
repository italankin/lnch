package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.RemovableItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;

public final class DeepShortcutViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem,
        RemovableItem, VisibleItem {
    public final String packageName;
    public final String id;
    public final boolean enabled;
    private final DeepShortcutDescriptor descriptor;
    private final String label;
    private final int color;
    private String customLabel;
    private Integer customColor;
    private boolean visible = true;

    public DeepShortcutViewModel(DeepShortcutDescriptor descriptor) {
        this.descriptor = descriptor;
        this.packageName = descriptor.packageName;
        this.id = descriptor.id;
        this.enabled = descriptor.enabled;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public DeepShortcutDescriptor getDescriptor() {
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
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    @Override
    public boolean is(DescriptorItem another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        DeepShortcutViewModel that = (DeepShortcutViewModel) another;
        return this.getDescriptor().getId().equals(that.getDescriptor().getId())
                && this.isVisible() == that.isVisible();
    }

    @Override
    public boolean deepEquals(DescriptorItem another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        DeepShortcutViewModel that = (DeepShortcutViewModel) another;
        if (this.customLabel != null
                ? !this.customLabel.equals(that.customLabel)
                : that.customLabel != null) {
            return false;
        }
        if (this.customColor != null
                ? !this.customColor.equals(that.customColor)
                : that.customColor != null) {
            return false;
        }
        return this.visible == that.visible;
    }
}
