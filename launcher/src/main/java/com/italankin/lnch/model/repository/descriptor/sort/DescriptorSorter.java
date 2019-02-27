package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

interface DescriptorSorter {

    boolean sort(List<Descriptor> descriptors);
}
