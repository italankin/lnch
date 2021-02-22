package com.italankin.lnch.model.repository.search.match;

import com.italankin.lnch.model.descriptor.Descriptor;

public class PartialDescriptorMatch extends PartialMatch implements DescriptorMatch {

    private final Descriptor descriptor;
    private final Kind kind;

    public PartialDescriptorMatch(Descriptor descriptor, Type type, Kind kind) {
        super(type);
        this.descriptor = descriptor;
        this.kind = kind;
        actions.add(Action.INFO);
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public Kind getKind() {
        return kind;
    }
}
