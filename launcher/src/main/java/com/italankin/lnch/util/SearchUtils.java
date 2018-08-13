package com.italankin.lnch.util;

import java.util.Locale;

public final class SearchUtils {

    public static boolean contains(String what, String substring) {
        return what != null && substring != null && what.toLowerCase(Locale.getDefault()).contains(substring);
    }

    public static boolean containsWord(String what, String word) {
        if (what == null || word == null) {
            return false;
        }
        String[] words = what.toLowerCase().split("\\s+");
        for (String w : words) {
            if (w.startsWith(word)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(String what, String prefix) {
        return what != null && prefix != null && what.toLowerCase(Locale.getDefault()).startsWith(prefix);
    }

    private SearchUtils() {
        // no instance
    }
}
