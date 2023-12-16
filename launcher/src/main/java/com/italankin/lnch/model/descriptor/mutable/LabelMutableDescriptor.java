package com.italankin.lnch.model.descriptor.mutable;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface LabelMutableDescriptor<T extends Descriptor> extends MutableDescriptor<T> {

    void setLabel(String label);

    String getLabel();
}
