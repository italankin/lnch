package com.italankin.lnch.model.descriptor.mutable;

import androidx.annotation.ColorInt;
import com.italankin.lnch.model.descriptor.Descriptor;

public interface ColorMutableDescriptor<T extends Descriptor> extends MutableDescriptor<T> {

    void setColor(@ColorInt int color);

    int getColor();
}
