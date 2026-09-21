package com.italankin.lnch.model.descriptor.mutable;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

public interface CustomLayoutMutableDescriptor<T extends Descriptor> extends MutableDescriptor<T> {

    void setCustomWidth(@Nullable ItemWidth width);

    @Nullable
    ItemWidth getCustomWidth();

    void setCustomTextAlign(@Nullable TextAlign textAlign);

    @Nullable
    TextAlign getCustomTextAlign();
}
