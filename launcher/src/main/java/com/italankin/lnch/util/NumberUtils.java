package com.italankin.lnch.util;

public final class NumberUtils {

    public static Integer parseInt(String s) {
        return parseInt(s, null);
    }

    public static Integer parseInt(String s, Integer defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private NumberUtils() {
        // no instance
    }
}
