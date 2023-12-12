package com.italankin.lnch.feature.home.repository;

import androidx.annotation.NonNull;

public enum EditModeChangeEvent implements HomeBus.Event {
    ENTER,
    COMMIT,
    DISCARD;

    @NonNull
    @Override
    public String toString() {
        return "EditModeChangeEvent." + name();
    }
}
