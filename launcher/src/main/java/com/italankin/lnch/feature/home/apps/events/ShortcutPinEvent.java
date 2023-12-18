package com.italankin.lnch.feature.home.apps.events;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;

public class ShortcutPinEvent {
    public final Shortcut shortcut;
    public final boolean pinned;

    public ShortcutPinEvent(Shortcut shortcut, boolean pinned) {
        this.shortcut = shortcut;
        this.pinned = pinned;
    }
}
