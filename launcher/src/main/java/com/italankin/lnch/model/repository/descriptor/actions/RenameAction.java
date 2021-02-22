package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

public class RenameAction extends BaseAction {
    private final String id;
    private final String newLabel;

    public RenameAction(CustomLabelDescriptor descriptor, String newLabel) {
        this.id = descriptor.getId();
        this.newLabel = newLabel;
    }

    @Override
    public void apply(List<Descriptor> items) {
        CustomLabelDescriptor descriptor = findById(items, id);
        descriptor.setCustomLabel(newLabel);
    }
}
