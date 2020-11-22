package com.italankin.lnch.model.viewmodel;

import com.italankin.lnch.model.descriptor.Descriptor;

/**
 * Visual representation of {@link Descriptor}
 */
public interface DescriptorItem {

    /**
     * @return descriptor this item represents
     */
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

    default Object getChangePayload(DescriptorItem oldItem) {
        return null;
    }
}
