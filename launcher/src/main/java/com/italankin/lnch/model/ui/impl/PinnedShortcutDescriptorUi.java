package com.italankin.lnch.model.ui.impl;

import androidx.annotation.NonNull;

import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;

import java.util.Objects;

public final class PinnedShortcutDescriptorUi implements DescriptorUi,
        CustomLabelDescriptorUi,
        CustomColorDescriptorUi,
        RemovableDescriptorUi,
        InFolderDescriptorUi,
        IgnorableDescriptorUi {

    public final String uri;
    private final PinnedShortcutDescriptor descriptor;
    private final String label;
    private final int color;
    private String customLabel;
    private Integer customColor;
    private boolean ignored;

    public PinnedShortcutDescriptorUi(PinnedShortcutDescriptor descriptor) {
        this.descriptor = descriptor;
        this.uri = descriptor.uri;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.ignored = descriptor.ignored;
    }

    @Override
    public PinnedShortcutDescriptor getDescriptor() {
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
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @NonNull
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
        PinnedShortcutDescriptorUi that = (PinnedShortcutDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && this.ignored == that.ignored;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        PinnedShortcutDescriptorUi that = (PinnedShortcutDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && Objects.equals(this.customLabel, that.customLabel)
                && Objects.equals(this.customColor, that.customColor)
                && this.ignored == that.ignored;
    }
}
