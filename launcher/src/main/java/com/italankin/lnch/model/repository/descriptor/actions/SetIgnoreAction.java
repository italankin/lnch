package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class SetIgnoreAction extends BaseAction {
    private final String id;
    private final boolean ignored;

    public SetIgnoreAction(IgnorableDescriptor descriptor, boolean ignored) {
        this(descriptor.getId(), ignored);
    }

    public SetIgnoreAction(String descriptorId, boolean ignored) {
        this.id = descriptorId;
        this.ignored = ignored;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        IgnorableMutableDescriptor<?> descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setIgnored(ignored);
        }
    }
}
