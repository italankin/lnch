package com.italankin.lnch.model.descriptor;

/**
 * A descriptor which can have a custom label
 */
public interface CustomLabelDescriptor extends LabelDescriptor {

    void setCustomLabel(String label);

    String getCustomLabel();

    default String getVisibleLabel() {
        String customLabel = getCustomLabel();
        return customLabel != null ? customLabel : getLabel();
    }
}
