package com.italankin.lnch.model.viewmodel;

import android.support.annotation.ColorInt;

/**
 * Item which has color
 */
public interface ColorItem extends DescriptorItem {

    @ColorInt
    int getColor();
}
