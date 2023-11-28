package com.italankin.lnch.model.descriptor;

/**
 * Base interface for all items which are displayed on home page
 */
public interface Descriptor {

    /**
     * @return unique identifier for this descriptor
     */
    String getId();

    String getOriginalLabel();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    /**
     * @return a deep copy of this descriptor
     */
    Descriptor copy();
}
