package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class RenameAction implements DescriptorRepository.Editor.Action {
    private final String id;
    private final String newLabel;

    public RenameAction(CustomLabelDescriptor descriptor, String newLabel) {
        this.id = descriptor.getId();
        this.newLabel = newLabel;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                ((CustomLabelDescriptor) item).setCustomLabel(newLabel);
                break;
            }
        }
    }
}
