package com.italankin.lnch.util.widget.colorpicker;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

interface ColorModelController {

    void init(ViewGroup root, LayoutInflater inflater);

    void destroy();

    void setColor(@ColorInt int color);

    @ColorInt
    int getColor();

    void setListener(@Nullable ColorPickerView.OnColorChangedListener listener);
}
