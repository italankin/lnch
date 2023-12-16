package com.italankin.lnch.model.descriptor;

/**
 * A descriptor which can have a custom label
 */
public interface CustomLabelDescriptor extends LabelDescriptor {

    String getCustomLabel();

    default String getVisibleLabel() {
        String customLabel = getCustomLabel();
        return customLabel != null ? customLabel : getLabel();
    }
}
