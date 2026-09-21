package com.italankin.lnch.model.repository.descriptor.actions;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.CustomLayoutDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLayoutMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

import java.util.List;

public class SetLayoutAction extends BaseAction {
    private final String id;
    @Nullable
    private final ItemWidth width;
    @Nullable
    private final TextAlign textAlign;

    public SetLayoutAction(CustomLayoutDescriptor descriptor, @Nullable ItemWidth width, @Nullable TextAlign textAlign) {
        this.id = descriptor.getId();
        this.width = width;
        this.textAlign = textAlign;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        CustomLayoutMutableDescriptor<?> descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setCustomWidth(width);
            descriptor.setCustomTextAlign(textAlign);
        }
    }
}
