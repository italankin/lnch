package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.ShortcutDescriptor;

public class ShortcutViewModel implements ItemViewModel {
    public final ShortcutDescriptor item;
    public final String uri;
    public final String label;
    public String customLabel;
    public int color;
    public Integer customColor;

    public ShortcutViewModel(ShortcutDescriptor item) {
        this.item = item;
        this.uri = item.uri;
        this.label = item.getVisibleLabel();
        this.color = item.getVisibleColor();
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
}
