package com.italankin.lnch.model.repository.descriptor.apps;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.ArrayList;
import java.util.List;

public class AppsData {

    public static AppsData create(List<MutableDescriptor<?>> items, boolean changed) {
        List<Descriptor> descriptors = new ArrayList<>(items.size());
        for (MutableDescriptor<?> item : items) {
            descriptors.add(item.toDescriptor());
        }
        return new AppsData(descriptors, changed);
    }

    public final List<Descriptor> items;
    public final boolean changed;

    public AppsData(List<Descriptor> items, boolean changed) {
        this.changed = changed;
        this.items = new ArrayList<>(items);
    }

    public AppsData copy(boolean changed) {
        return new AppsData(items, changed || this.changed);
    }
}
