package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface Match {

    Uri getIcon();

    @DrawableRes
    int getIconResource();

    CharSequence getLabel();

    @ColorInt
    int getColor();

    String toString();

    Intent getIntent();

    Descriptor getDescriptor();
}

