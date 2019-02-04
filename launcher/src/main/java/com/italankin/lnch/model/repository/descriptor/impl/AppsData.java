package com.italankin.lnch.model.repository.descriptor.impl;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

class AppsData {
    final List<Descriptor> items;
    final boolean changed;

    AppsData(List<Descriptor> items, boolean changed) {
        this.items = items;
        this.changed = changed;
    }
}
