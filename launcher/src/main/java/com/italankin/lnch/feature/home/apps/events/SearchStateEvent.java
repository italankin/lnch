package com.italankin.lnch.feature.home.apps.events;

import androidx.annotation.NonNull;
import com.italankin.lnch.feature.home.repository.HomeBus;

public enum SearchStateEvent implements HomeBus.Event {
    SHOWN,
    HIDDEN;

    @NonNull
    @Override
    public String toString() {
        return "SearchStateEvent." + name();
    }
}
