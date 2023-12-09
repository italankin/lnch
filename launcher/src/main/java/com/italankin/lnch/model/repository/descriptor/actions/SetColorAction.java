package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class SetColorAction extends BaseAction {
    private final String id;
    private final Integer newColor;

    public SetColorAction(CustomColorDescriptor descriptor, Integer newColor) {
        this.id = descriptor.getId();
        this.newColor = newColor;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        CustomColorMutableDescriptor<?> descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setCustomColor(newColor);
        }
    }
}
