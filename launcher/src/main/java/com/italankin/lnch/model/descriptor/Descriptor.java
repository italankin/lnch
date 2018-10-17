package com.italankin.lnch.model.descriptor;

public interface Descriptor {

    String getId();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
