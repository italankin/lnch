package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

public interface Match {

    Drawable getIcon();

    @DrawableRes
    int getIconResource();

    CharSequence getLabel();

    @ColorInt
    int getColor();

    String toString();

    Intent getIntent();

}

