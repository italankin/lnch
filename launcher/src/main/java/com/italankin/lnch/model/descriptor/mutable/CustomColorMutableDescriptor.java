package com.italankin.lnch.model.descriptor.mutable;

import androidx.annotation.ColorInt;
import com.italankin.lnch.model.descriptor.Descriptor;

public interface CustomColorMutableDescriptor<T extends Descriptor> extends ColorMutableDescriptor<T> {

    void setCustomColor(@ColorInt Integer customColor);

    Integer getCustomColor();
}
