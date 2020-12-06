package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class RecolorAction implements DescriptorRepository.Editor.Action {
    private final String id;
    private final Integer newColor;

    public RecolorAction(CustomColorDescriptor descriptor, Integer newColor) {
        this.id = descriptor.getId();
        this.newColor = newColor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                ((CustomColorDescriptor) item).setCustomColor(newColor);
                break;
            }
        }
    }
}
