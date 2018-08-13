package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;

public class AppViewModel implements ItemViewModel {
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
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public String getVisibleLabel() {
        if (customLabel != null) {
            return customLabel;
        }
        return label;
    }

    public int getVisibleColor() {
        if (customColor != null) {
            return customColor;
        }
        return color;
    }

    public boolean isVisible() {
        return !hidden && visible;
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
