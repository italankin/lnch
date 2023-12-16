package com.italankin.lnch.feature.settings.apps.details;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

class AppDetailsModel {

    final AppDescriptor descriptor;
    boolean ignored;
    String customLabel;
    Integer customColor;
    int searchFlags;
    boolean showShortcuts;
    Integer customBadgeColor;

    public AppDetailsModel(AppDescriptor descriptor) {
        this.descriptor = descriptor;
        ignored = descriptor.ignored;
        customLabel = descriptor.customLabel;
        customColor = descriptor.customColor;
        searchFlags = descriptor.searchFlags;
        showShortcuts = descriptor.showShortcuts;
        customBadgeColor = descriptor.customBadgeColor;
    }

    public String getVisibleLabel() {
        return customLabel != null ? customLabel : descriptor.getLabel();
    }

    public int getVisibleColor() {
        return customColor != null ? customColor : descriptor.getColor();
    }
}
