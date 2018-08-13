package com.italankin.lnch.util;

import java.util.Collections;
import java.util.List;

public final class ListUtils {

    public static <T> void swap(List<T> items, int from, int to) {
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
