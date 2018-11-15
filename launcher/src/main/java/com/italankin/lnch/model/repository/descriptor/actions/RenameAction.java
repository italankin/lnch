package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class RenameAction implements DescriptorRepository.Editor.Action {
    private final CustomLabelDescriptor item;
    private final String customLabel;

    public RenameAction(CustomLabelDescriptor item, String customLabel) {
        this.item = item;
        this.customLabel = customLabel;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.item.equals(item)) {
                ((CustomLabelDescriptor) item).setCustomLabel(customLabel);
                break;
            }
        }
    }
}
