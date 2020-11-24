package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class SetVisibilityAction implements DescriptorRepository.Editor.Action {
    private final IgnorableDescriptor item;
    private final boolean visible;

    public SetVisibilityAction(IgnorableDescriptor item, boolean visible) {
        this.item = item;
        this.visible = visible;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.item.equals(item)) {
                ((IgnorableDescriptor) item).setIgnored(!visible);
                break;
            }
        }
    }
}
