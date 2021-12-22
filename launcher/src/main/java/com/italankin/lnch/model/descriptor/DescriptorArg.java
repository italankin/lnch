package com.italankin.lnch.model.descriptor;

import java.io.Serializable;

public final class DescriptorArg implements Serializable {

    public final String id;
    public final Class<? extends Descriptor> type;

    public DescriptorArg(Descriptor descriptor) {
        this(descriptor.getId(), descriptor.getClass());
    }

    public DescriptorArg(String id) {
        this(id, Descriptor.class);
    }

    public DescriptorArg(String id, Class<? extends Descriptor> type) {
        this.id = id;
        this.type = type;
    }

    public boolean is(Descriptor descriptor) {
        return descriptor.getId().equals(id) && type.isAssignableFrom(descriptor.getClass());
    }
}
