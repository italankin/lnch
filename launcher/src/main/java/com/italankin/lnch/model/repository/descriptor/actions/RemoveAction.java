package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.Iterator;
import java.util.List;

public class RemoveAction implements DescriptorRepository.Editor.Action {
    private final String id;

    public RemoveAction(String id) {
        this.id = id;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        for (Iterator<MutableDescriptor<?>> i = items.iterator(); i.hasNext(); ) {
            if (i.next().getId().equals(id)) {
                i.remove();
                break;
            }
        }
    }
}
