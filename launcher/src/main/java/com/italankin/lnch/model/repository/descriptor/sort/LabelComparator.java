package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.util.DescriptorUtils;

import java.util.Comparator;

class LabelComparator implements Comparator<Descriptor> {

    private final boolean asc;
    private boolean changed;

    LabelComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(Descriptor d1, Descriptor d2) {
        String lhs = DescriptorUtils.getVisibleLabel(d1);
        String rhs = DescriptorUtils.getVisibleLabel(d2);
        int result;
        if (lhs.isEmpty() || rhs.isEmpty()) {
            result = lhs.isEmpty() ? 1 : -1;
        } else if (asc) {
            result = String.CASE_INSENSITIVE_ORDER.compare(lhs, rhs);
        } else {
            result = String.CASE_INSENSITIVE_ORDER.compare(rhs, lhs);
        }
        changed = changed || result != 0;
        return result;
    }

    boolean isChanged() {
        return changed;
    }
}
