package com.italankin.lnch.model.descriptor;

public interface Descriptor {

    /**
     * @return unique identifier for this descriptor
     */
    String getId();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
