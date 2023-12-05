package com.italankin.lnch.feature.widgets.model;

import androidx.annotation.Nullable;

public class NoWidgetsItem implements WidgetAdapterItem {

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof NoWidgetsItem;
    }
}
