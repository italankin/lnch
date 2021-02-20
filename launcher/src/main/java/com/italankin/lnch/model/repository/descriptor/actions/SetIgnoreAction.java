package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class SetIgnoreAction implements DescriptorRepository.Editor.Action {
    private final String id;
    private final boolean ignored;

    public SetIgnoreAction(IgnorableDescriptor descriptor, boolean ignored) {
        this.id = descriptor.getId();
        this.ignored = ignored;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                ((IgnorableDescriptor) item).setIgnored(ignored);
                break;
            }
        }
    }
}
