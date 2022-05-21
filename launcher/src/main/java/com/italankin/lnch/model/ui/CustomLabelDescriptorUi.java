package com.italankin.lnch.model.ui;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;

/**
 * Item with custom label which can be changed
 */
public interface CustomLabelDescriptorUi extends LabelDescriptorUi {

    static String getVisibleLabel(DescriptorUi descriptor) {
        if (descriptor instanceof CustomLabelDescriptorUi) {
            return ((CustomLabelDescriptorUi) descriptor).getVisibleLabel();
        }
        return null;
    }

    @Override
    CustomLabelDescriptor getDescriptor();

    void setCustomLabel(String label);

    String getCustomLabel();

    default String getVisibleLabel() {
        String customLabel = getCustomLabel();
        return customLabel != null ? customLabel : getLabel();
    }
}
