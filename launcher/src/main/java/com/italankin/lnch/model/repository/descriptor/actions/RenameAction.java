package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class RenameAction extends BaseAction {
    private final String id;
    private final String newLabel;

    public RenameAction(CustomLabelDescriptor descriptor, String newLabel) {
        this.id = descriptor.getId();
        this.newLabel = newLabel;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        CustomLabelMutableDescriptor<?> descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setCustomLabel(newLabel);
        }
    }
}
