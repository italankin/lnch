package com.italankin.lnch.model.ui;

import com.italankin.lnch.model.descriptor.ColorDescriptor;

import androidx.annotation.ColorInt;

/**
 * Item which has color
 */
public interface ColorDescriptorUi extends DescriptorUi {

    @Override
    ColorDescriptor getDescriptor();

    @ColorInt
    int getColor();
}
