package com.italankin.lnch.model.repository.search.match;

import com.italankin.lnch.model.descriptor.Descriptor;

/**
 * A match for {@link Descriptor}
 */
public interface DescriptorMatch extends Match {

    Descriptor getDescriptor();
}
