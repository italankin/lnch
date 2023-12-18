package com.italankin.lnch.feature.home.repository.editmode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.feature.home.repository.EditModeState;

class SimpleProperty<T> implements EditModeState.Property<T> {
    private final String key;

    SimpleProperty(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SimpleProperty) {
            return key.equals(((SimpleProperty<?>) obj).key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return key;
    }
}
