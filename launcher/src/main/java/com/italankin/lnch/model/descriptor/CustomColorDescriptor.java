package com.italankin.lnch.model.descriptor;

import android.support.annotation.ColorInt;

public interface CustomColorDescriptor extends ColorDescriptor {

    void setCustomColor(Integer color);

    Integer getCustomColor();

    @ColorInt
    int getVisibleColor();
}
