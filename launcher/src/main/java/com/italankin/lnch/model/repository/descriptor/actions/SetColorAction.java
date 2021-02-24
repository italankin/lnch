package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

public class SetColorAction extends BaseAction {
    private final String id;
    private final Integer newColor;

    public SetColorAction(CustomColorDescriptor descriptor, Integer newColor) {
        this.id = descriptor.getId();
        this.newColor = newColor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        CustomColorDescriptor descriptor = findById(items, id);
        descriptor.setCustomColor(newColor);
    }
}
