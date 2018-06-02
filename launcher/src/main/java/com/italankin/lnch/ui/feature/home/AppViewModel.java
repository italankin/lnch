package com.italankin.lnch.ui.feature.home;

import com.italankin.lnch.bean.AppItem;

public class AppViewModel {
    public final AppItem item;
    public final String packageName;
    public final int color;
    public final String label;
    public String customLabel;
    public Integer customColor;

    public AppViewModel(AppItem item) {
        this.item = item;
        this.packageName = item.packageName;
        this.label = item.label;
        this.customLabel = item.customLabel;
        this.color = item.color;
        this.customColor = item.customColor;
    }

    public String getLabel() {
        return item.getLabel();
    }

    public int getColor() {
        return item.getColor();
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
