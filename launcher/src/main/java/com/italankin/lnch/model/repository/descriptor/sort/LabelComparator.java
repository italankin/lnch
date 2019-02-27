package com.italankin.lnch.model.repository.descriptor.sort;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;

import java.util.Comparator;

class LabelComparator implements Comparator<Descriptor> {
    private final boolean asc;
    private boolean changed;

    LabelComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(Descriptor d1, Descriptor d2) {
        String lhs = getLabel(d1);
        String rhs = getLabel(d2);
        int result;
        if (lhs == null || rhs == null) {
            result = lhs == null ? 1 : -1;
        } else if (asc) {
            result = String.CASE_INSENSITIVE_ORDER.compare(lhs, rhs);
        } else {
            result = String.CASE_INSENSITIVE_ORDER.compare(rhs, lhs);
        }
        changed = result != 0;
        return result;
    }

    boolean isChanged() {
        return changed;
    }

    private static String getLabel(Descriptor descriptor) {
        if (descriptor instanceof CustomLabelDescriptor) {
            return ((CustomLabelDescriptor) descriptor).getVisibleLabel();
        }
        if (descriptor instanceof LabelDescriptor) {
            return ((LabelDescriptor) descriptor).getLabel();
        }
        return null;
    }
}
