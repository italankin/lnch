package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.bean.AppItem;

public class AppViewModel {
    public final AppItem item;
    public final String packageName;
    public final String componentName;
    public final int color;
    public final String label;
    public boolean hidden;
    public boolean visible = true;
    public String customLabel;
    public Integer customColor;

    public AppViewModel(AppItem item) {
        this.item = item;
        this.packageName = item.id;
        this.componentName = item.componentName;
        this.label = item.label;
        this.hidden = item.hidden;
        this.visible = !item.hidden;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
    }

    public String getLabel() {
        if (customLabel != null) {
            return customLabel;
        }
        return label;
    }

    public int getColor() {
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
