package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.bean.AppItem;

public class AppViewModel {
    public final AppItem item;
    public final String packageName;
    public final int color;
    public final String label;
    public boolean hidden;
    public String customLabel;
    public Integer customColor;

    public AppViewModel(AppItem item) {
        this.item = item;
        this.packageName = item.packageName;
        this.label = item.label;
        this.hidden = item.hidden;
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

    @Override
    public String toString() {
        return item.toString();
    }
}
