package com.italankin.lnch.model.descriptor;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

public interface CustomLayoutDescriptor extends Descriptor {

    @Nullable
    ItemWidth getCustomWidth();

    @Nullable
    TextAlign getCustomTextAlign();
}
