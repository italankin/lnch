package com.italankin.lnch.model.descriptor;

import androidx.annotation.ColorInt;

/**
 * A descriptor which can have a custom text color
 */
public interface CustomColorDescriptor extends ColorDescriptor {

    void setCustomColor(@ColorInt Integer color);

    @ColorInt
    Integer getCustomColor();

    @ColorInt
    default int getVisibleColor() {
        Integer customColor = getCustomColor();
        return customColor != null ? customColor : getColor();
    }
}
