package com.italankin.lnch.model.repository.search.match;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PartialDescriptorMatch extends PartialMatch implements DescriptorMatch {

    private static final Set<Action> ACTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Action.START, Action.INFO)));

    public Descriptor descriptor;

    public PartialDescriptorMatch(Type type) {
        super(type);
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public Set<Action> availableActions() {
        return ACTIONS;
    }
}
