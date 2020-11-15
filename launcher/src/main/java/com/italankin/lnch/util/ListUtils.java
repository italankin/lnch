package com.italankin.lnch.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

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

    public static <T> List<T> reversedCopy(List<T> list) {
        List<T> reversed = new ArrayList<>(list.size());
        ListIterator<T> i = list.listIterator(list.size());
        while (i.hasPrevious()) {
            reversed.add(i.previous());
        }
        return reversed;
    }

    private ListUtils() {
        // no instance
    }
}
