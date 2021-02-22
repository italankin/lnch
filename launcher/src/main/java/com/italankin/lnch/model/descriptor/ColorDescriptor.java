package com.italankin.lnch.model.descriptor;

import androidx.annotation.ColorInt;

/**
 * A descriptor which has a color
 */
public interface ColorDescriptor extends Descriptor {

    @ColorInt
    int getColor();
}
