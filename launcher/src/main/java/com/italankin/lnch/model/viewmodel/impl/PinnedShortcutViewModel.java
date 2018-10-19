package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.GroupedItem;
import com.italankin.lnch.model.viewmodel.RemovableItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;

public class PinnedShortcutViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem,
        GroupedItem, RemovableItem, VisibleItem {
    public final String uri;
    private final PinnedShortcutDescriptor descriptor;
    private final String label;
    private String customLabel;
    private int color;
    private Integer customColor;
    private boolean visible = true;

    public PinnedShortcutViewModel(PinnedShortcutDescriptor descriptor) {
        this.descriptor = descriptor;
        this.uri = descriptor.uri;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
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
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }
}
