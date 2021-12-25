package com.italankin.lnch.feature.home.repository;

import com.italankin.lnch.model.ui.DescriptorUi;

public final class HomeEntry<T extends DescriptorUi> {

    public final int position;
    public final T item;

    HomeEntry(int position, T item) {
        this.position = position;
        this.item = item;
    }
}
