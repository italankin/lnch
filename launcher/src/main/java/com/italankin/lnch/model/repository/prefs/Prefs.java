package com.italankin.lnch.model.repository.prefs;

final class Prefs {

    static <T> Preferences.Pref<T> create(String key, T defaultValue) {
        return new BasePref<>(key, defaultValue);
    }

    static <T> Preferences.RangePref<T> create(String key, T defaultValue, T min, T max) {
        return new RangePref<>(key, defaultValue, min, max);
    }

    private static class BasePref<T> implements Preferences.Pref<T> {
        private final String key;
        private final T defaultValue;

        BasePref(String key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public T defaultValue() {
            return defaultValue;
        }
    }

    private static class RangePref<T> extends BasePref<T> implements Preferences.RangePref<T> {
        private final T min;
        private final T max;

        private RangePref(String key, T defaultValue, T min, T max) {
            super(key, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public T min() {
            return min;
        }

        @Override
        public T max() {
            return max;
        }
    }

    private Prefs() {
        // no instance
    }
}
