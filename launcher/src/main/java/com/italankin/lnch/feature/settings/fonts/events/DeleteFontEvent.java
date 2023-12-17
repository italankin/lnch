package com.italankin.lnch.feature.settings.fonts.events;

public class DeleteFontEvent {
    public final boolean reset;

    public DeleteFontEvent(boolean reset) {
        this.reset = reset;
    }
}
