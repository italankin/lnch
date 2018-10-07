package com.italankin.lnch.feature.home.descriptor;

import android.support.annotation.ColorInt;

/**
 * Item with custom color which can be changed
 */
public interface CustomColorItem extends ColorItem {
    void setCustomColor(@ColorInt Integer color);

    Integer getCustomColor();
}
