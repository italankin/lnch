package com.italankin.lnch.feature.widgets.events;

import androidx.annotation.NonNull;
import com.italankin.lnch.feature.home.repository.HomeBus;

public enum WidgetEditModeChangeEvent implements HomeBus.Event {
    ENTER,
    EXIT;

    @NonNull
    @Override
    public String toString() {
        return "WidgetEditModeChangeEvent." + name();
    }
}
