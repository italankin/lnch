package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.Iterator;
import java.util.List;

public class RemoveAction implements DescriptorRepository.Editor.Action {
    private final String id;

    public RemoveAction(Descriptor descriptor) {
        this.id = descriptor.getId();
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Iterator<Descriptor> i = items.iterator(); i.hasNext(); ) {
            if (i.next().getId().equals(id)) {
                i.remove();
                break;
            }
        }
    }
}
