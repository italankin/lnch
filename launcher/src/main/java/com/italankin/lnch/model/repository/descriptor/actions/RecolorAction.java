package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

public class RecolorAction extends BaseAction {
    private final String id;
    private final Integer newColor;

    public RecolorAction(CustomColorDescriptor descriptor, Integer newColor) {
        this.id = descriptor.getId();
        this.newColor = newColor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        CustomColorDescriptor descriptor = findById(items, id);
        descriptor.setCustomColor(newColor);
    }
}
