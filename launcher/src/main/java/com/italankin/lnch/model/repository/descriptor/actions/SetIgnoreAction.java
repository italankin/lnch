package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;

import java.util.List;

public class SetIgnoreAction extends BaseAction {
    private final String id;
    private final boolean ignored;

    public SetIgnoreAction(IgnorableDescriptor descriptor, boolean ignored) {
        this.id = descriptor.getId();
        this.ignored = ignored;
    }

    @Override
    public void apply(List<Descriptor> items) {
        IgnorableDescriptor descriptor = findById(items, id);
        descriptor.setIgnored(ignored);
    }
}
