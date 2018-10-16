package com.italankin.lnch.feature.home.descriptor.model;

import com.italankin.lnch.feature.home.descriptor.CustomColorItem;
import com.italankin.lnch.feature.home.descriptor.CustomLabelItem;
import com.italankin.lnch.feature.home.descriptor.DescriptorItem;
import com.italankin.lnch.feature.home.descriptor.GroupedItem;
import com.italankin.lnch.feature.home.descriptor.RemovableItem;
import com.italankin.lnch.feature.home.descriptor.VisibleItem;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.ShortcutDescriptor;

public class ShortcutViewModel implements DescriptorItem, CustomLabelItem, CustomColorItem,
        GroupedItem, RemovableItem, VisibleItem {
    public final ShortcutDescriptor item;
    public final String uri;
    public final String label;
    public String customLabel;
    public int color;
    public Integer customColor;
    public boolean visible = true;

    public ShortcutViewModel(ShortcutDescriptor item) {
        this.item = item;
        this.uri = item.uri;
        this.label = item.label;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
    }

    @Override
    public Descriptor getDescriptor() {
        return item;
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
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }
}
