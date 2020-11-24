package com.italankin.lnch.model.descriptor;

/**
 * Base interface for all items which are displayed on home page
 */
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
