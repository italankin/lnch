package com.italankin.lnch.model.ui;

/**
 * Item with custom label which can be changed
 */
public interface CustomLabelDescriptorUi extends LabelDescriptorUi {

    void setCustomLabel(String label);

    String getCustomLabel();

    default String getVisibleLabel() {
        String customLabel = getCustomLabel();
        return customLabel != null ? customLabel : getLabel();
    }
}
