package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

interface DescriptorSorter {

    /**
     * @return {@code true}, if list was changed
     */
    boolean sort(List<Descriptor> descriptors);
}
