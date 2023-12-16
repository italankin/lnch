package com.italankin.lnch.model.descriptor.mutable;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface CustomLabelMutableDescriptor<T extends Descriptor> extends LabelMutableDescriptor<T> {

    void setCustomLabel(String customLabel);

    String getCustomLabel();

    default String getVisibleLabel() {
        String customLabel = getCustomLabel();
        return customLabel != null ? customLabel : getLabel();
    }
}
