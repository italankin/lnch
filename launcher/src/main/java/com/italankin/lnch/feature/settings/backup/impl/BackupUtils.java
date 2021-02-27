package com.italankin.lnch.feature.settings.backup.impl;

import java.io.Closeable;
import java.io.IOException;

import timber.log.Timber;

final class BackupUtils {

    static final int DEFAULT_BUFFER_SIZE = 2 << 12;

    static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            Timber.e(e, "closeQuietly: %s", e.getMessage());
        }
    }

    private BackupUtils() {
        // no instance
    }
}
