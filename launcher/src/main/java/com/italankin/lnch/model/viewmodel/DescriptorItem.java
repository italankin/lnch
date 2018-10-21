package com.italankin.lnch.model.viewmodel;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface DescriptorItem {

    Descriptor getDescriptor();

    /**
     * @return {@code true} if this item shares the same {@link Descriptor#getId() id}
     * with {@code another}
     */
    boolean is(DescriptorItem another);

    /**
     * @return {@code true}, if this item has the same content as {@code another}
     */
    boolean deepEquals(DescriptorItem another);
}
