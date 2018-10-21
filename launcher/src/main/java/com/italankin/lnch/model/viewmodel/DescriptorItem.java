package com.italankin.lnch.model.viewmodel;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface DescriptorItem {

    Descriptor getDescriptor();

    boolean is(DescriptorItem another);

    boolean deepEquals(DescriptorItem another);
}
