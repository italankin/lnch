package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.List;

public class AddAction implements AppsRepository.Editor.Action {
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
