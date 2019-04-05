package com.italankin.lnch.model.repository.descriptor.apps;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

public class AppsData {
    public final List<Descriptor> items;
    public final boolean changed;

    public AppsData(List<Descriptor> items, boolean changed) {
        this.items = items;
        this.changed = changed;
    }

    public AppsData copy(boolean changed) {
        return new AppsData(items, changed || this.changed);
    }
}
