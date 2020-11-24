package com.italankin.lnch.model.ui;

import androidx.annotation.ColorInt;

/**
 * Item which has color
 */
public interface ColorDescriptorUi extends DescriptorUi {

    @ColorInt
    int getColor();
}
