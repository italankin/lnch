package com.italankin.lnch.model.repository.store.json.model;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface DescriptorJson {

    String PROPERTY_TYPE = "type";

    Descriptor toDescriptor();
}
