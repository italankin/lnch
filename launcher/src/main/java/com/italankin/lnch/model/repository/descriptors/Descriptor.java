package com.italankin.lnch.model.repository.descriptors;

public interface Descriptor {

    String getId();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
