package com.italankin.lnch.model.ui;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.CustomLayoutDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

public interface CustomLayoutDescriptorUi extends DescriptorUi {

    void setCustomWidth(@Nullable ItemWidth width);

    @Nullable
    ItemWidth getCustomWidth();

    void setCustomTextAlign(@Nullable TextAlign textAlign);

    @Nullable
    TextAlign getCustomTextAlign();

    @Override
    CustomLayoutDescriptor getDescriptor();
}
