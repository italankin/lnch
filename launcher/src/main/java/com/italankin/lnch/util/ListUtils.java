package com.italankin.lnch.util;

import java.util.Collections;
import java.util.List;

public final class ListUtils {

    public static void swap(List<?> items, int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
    }

    private ListUtils() {
        // no instance
    }
}
