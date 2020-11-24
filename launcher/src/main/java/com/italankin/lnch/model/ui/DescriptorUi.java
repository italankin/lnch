package com.italankin.lnch.model.ui;

import com.italankin.lnch.model.descriptor.Descriptor;

/**
 * Visual representation of {@link Descriptor}
 */
public interface DescriptorUi {

    /**
     * @return descriptor this item represents
     */
    Descriptor getDescriptor();

    /**
     * @return {@code true} if this item shares the same {@link Descriptor#getId() id}
     * with {@code another}
     */
    boolean is(DescriptorUi another);

    /**
     * @return {@code true}, if this item has the same content as {@code another}
     */
    boolean deepEquals(DescriptorUi another);

    default Object getChangePayload(DescriptorUi oldItem) {
        return null;
    }
}
