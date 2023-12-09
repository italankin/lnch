package com.italankin.lnch.model.descriptor.mutable;

import com.italankin.lnch.model.descriptor.Descriptor;

/**
 * Mutable version of {@link Descriptor}
 */
public interface MutableDescriptor<T extends Descriptor> {

    String getId();

    void setOriginalLabel(String originalLabel);

    String getOriginalLabel();

    T toDescriptor();
}
