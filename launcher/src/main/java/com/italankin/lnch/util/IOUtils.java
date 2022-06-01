package com.italankin.lnch.util;

import java.io.Closeable;
import java.io.IOException;

import timber.log.Timber;

public final class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            Timber.e(e, "closeQuietly: %s", e.getMessage());
        }
    }

    private IOUtils() {
        // no instance
    }
}
