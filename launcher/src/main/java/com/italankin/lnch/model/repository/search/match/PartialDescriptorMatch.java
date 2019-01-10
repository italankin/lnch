package com.italankin.lnch.model.repository.search.match;

import com.italankin.lnch.model.descriptor.Descriptor;

public class PartialDescriptorMatch extends PartialMatch implements DescriptorMatch {

    public Descriptor descriptor;

    public PartialDescriptorMatch(Type type) {
        super(type);
        actions.add(Action.INFO);
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }
}
