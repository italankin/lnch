package com.italankin.lnch.feature.home.repository;

import com.italankin.lnch.model.ui.DescriptorUi;

public final class DescriptorUiEntry<T extends DescriptorUi> {

    public final int position;
    public final T item;

    DescriptorUiEntry(int position, T item) {
        this.position = position;
        this.item = item;
    }
}
