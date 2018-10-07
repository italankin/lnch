package com.italankin.lnch.feature.home.descriptor;

import android.support.annotation.ColorInt;

/**
 * Item which has color
 */
public interface ColorItem extends DescriptorItem {

    @ColorInt
    int getVisibleColor();

}
