package com.italankin.lnch.model.viewmodel;

import androidx.annotation.ColorInt;

/**
 * Item with custom color which can be changed
 */
public interface CustomColorItem extends ColorItem {

    void setCustomColor(@ColorInt Integer color);

    @ColorInt
    Integer getCustomColor();

    @ColorInt
    default int getVisibleColor() {
        Integer customColor = getCustomColor();
        return customColor != null ? customColor : getColor();
    }
}
