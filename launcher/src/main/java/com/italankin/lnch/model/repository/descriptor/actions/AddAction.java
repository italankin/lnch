package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class AddAction implements DescriptorRepository.Editor.Action {
    public static final int LAST = -1;

    private final int position;
    private final Descriptor descriptor;

    public AddAction(int position, Descriptor descriptor) {
        this.position = position;
        this.descriptor = descriptor;
    }

    public AddAction(Descriptor descriptor) {
        this(LAST, descriptor);
    }

    @Override
    public void apply(List<Descriptor> items) {
        if (position == LAST) {
            items.add(descriptor);
        } else {
            items.add(position, descriptor);
        }
    }
}
