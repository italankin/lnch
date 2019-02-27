package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.Collections;
import java.util.List;

public class DescLabelSorter implements DescriptorSorter {

    @Override
    public boolean sort(List<Descriptor> descriptors) {
        LabelComparator comparator = new LabelComparator(false);
        Collections.sort(descriptors, comparator);
        return comparator.isChanged();
    }
}
