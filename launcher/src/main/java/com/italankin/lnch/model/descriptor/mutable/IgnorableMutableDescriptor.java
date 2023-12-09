package com.italankin.lnch.model.descriptor.mutable;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface IgnorableMutableDescriptor<T extends Descriptor> extends MutableDescriptor<T> {

    void setIgnored(boolean ignored);

    boolean isIgnored();
}
