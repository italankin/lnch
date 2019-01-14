package com.italankin.lnch.model.descriptor;

import androidx.annotation.ColorInt;

public interface ColorDescriptor extends Descriptor {

    @ColorInt
    int getColor();
}
