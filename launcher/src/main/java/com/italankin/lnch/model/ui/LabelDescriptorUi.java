package com.italankin.lnch.model.ui;

import com.italankin.lnch.model.descriptor.LabelDescriptor;

/**
 * Item with visible label
 */
public interface LabelDescriptorUi extends DescriptorUi {

    @Override
    LabelDescriptor getDescriptor();

    String getLabel();
}
