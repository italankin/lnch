package com.italankin.lnch.feature.settings.backup.events;

import androidx.annotation.Nullable;

public class BackupActionEvent {
    @Nullable
    public final Throwable error;

    public BackupActionEvent(@Nullable Throwable error) {
        this.error = error;
    }
}
