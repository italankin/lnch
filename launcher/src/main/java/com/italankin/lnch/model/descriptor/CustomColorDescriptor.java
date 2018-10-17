package com.italankin.lnch.model.descriptor;

import android.support.annotation.ColorInt;

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
