package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.Collections;
import java.util.List;

public interface DescriptorSorter {

    DescriptorSorter LABEL_ASC = descriptors -> {
        LabelComparator comparator = new LabelComparator(true);
        Collections.sort(descriptors, comparator);
        return comparator.isChanged();
    };

    DescriptorSorter LABEL_DESC = descriptors -> {
        LabelComparator comparator = new LabelComparator(false);
        Collections.sort(descriptors, comparator);
        return comparator.isChanged();
    };

    /**
     * @return {@code true}, if list was changed
     */
    boolean sort(List<Descriptor> descriptors);
}
