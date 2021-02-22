package com.italankin.lnch.model.repository.prefs;

import android.content.SharedPreferences;

import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Factory for {@link com.italankin.lnch.model.repository.prefs.Preferences.Pref}
 */
final class Prefs {

    static Preferences.Pref<Integer> createInteger(String key, Integer defaultValue) {
        return new BasePref<>(key, defaultValue, new IntegerUpdater(key));
    }

    static Preferences.Pref<Boolean> createBoolean(String key, Boolean defaultValue) {
        return new BasePref<>(key, defaultValue, new BooleanUpdater(key));
    }

    static Preferences.Pref<String> createString(String key, String defaultValue) {
        return new BasePref<>(key, defaultValue, new StringUpdater(key));
    }

    static Preferences.RangePref<Float> createFloatRange(String key, Float defaultValue, Float min, Float max) {
        return new RangePref<>(key, defaultValue, min, max, new FloatUpdater(key));
    }

    static Preferences.RangePref<Integer> createIntegerRange(String key, Integer defaultValue, Integer min, Integer max) {
        return new RangePref<>(key, defaultValue, min, max, new IntegerUpdater(key));
    }

    static <T> Preferences.Pref<T> create(String key, T defaultValue, Fetcher2<T> fetcher2, Updater2<T> updater2) {
        return new BasePref<T>(
                key,
                preferences -> fetcher2.fetch(preferences, key, defaultValue),
                (preferences, newValue) -> updater2.update(preferences, key, newValue));
    }

    static <T> Preferences.Pref<T> create(String key, T defaultValue, ValueConverter<T> valueConverter) {
        return new BasePref<T>(
                key,
                preferences -> {
                    String value = preferences.getString(key, null);
                    return valueConverter.convert(value, defaultValue);
                },
                new ObjectUpdater<>(key)
        );
    }

    interface Fetcher2<T> {
        T fetch(SharedPreferences preferences, String key, T defaultValue);
    }

    interface Updater2<T> {
        void update(SharedPreferences preferences, String key, T newValue);
    }

    interface ValueConverter<T> {
        T convert(@Nullable String value, T defaultValue);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Impl
    ///////////////////////////////////////////////////////////////////////////

    private static class BasePref<T> implements Preferences.Pref<T> {
        private final String key;
        private final Fetcher<T> fetcher;
        private final Updater<T> updater;

        BasePref(String key, T defaultValue, Updater<T> updater) {
            this(key, new ObjectFetcher<>(key, defaultValue), updater);
        }

        BasePref(String key, Fetcher<T> fetcher, Updater<T> updater) {
            this.key = key;
            this.fetcher = fetcher;
            this.updater = updater;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Fetcher<T> fetcher() {
            return fetcher;
        }

        @Override
        public Updater<T> updater() {
            return updater;
        }
    }

    private static class RangePref<T> extends BasePref<T> implements Preferences.RangePref<T> {
        private final T min;
        private final T max;

        RangePref(String key, T defaultValue, T min, T max, Updater<T> updater) {
            super(key, defaultValue, updater);
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

    ///////////////////////////////////////////////////////////////////////////
    // Fetchers
    ///////////////////////////////////////////////////////////////////////////

    private static class ObjectFetcher<T> implements Preferences.Pref.Fetcher<T> {
        private final String key;
        private final T defaultValue;

        ObjectFetcher(String key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T fetch(SharedPreferences preferences) {
            Map<String, ?> map = preferences.getAll();
            Object value = map.get(key);
            return value == null ? defaultValue : (T) value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Updaters
    ///////////////////////////////////////////////////////////////////////////

    private static class ObjectUpdater<T> implements Preferences.Pref.Updater<T> {
        private final String key;

        ObjectUpdater(String key) {
            this.key = key;
        }

        @Override
        public void update(SharedPreferences preferences, T newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putString(key, newValue.toString()).apply();
            }
        }
    }

    private static class IntegerUpdater implements Preferences.Pref.Updater<Integer> {
        private final String key;

        IntegerUpdater(String key) {
            this.key = key;
        }

        @Override
        public void update(SharedPreferences preferences, Integer newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putInt(key, newValue).apply();
            }
        }
    }

    private static class FloatUpdater implements Preferences.Pref.Updater<Float> {
        private final String key;

        FloatUpdater(String key) {
            this.key = key;
        }

        @Override
        public void update(SharedPreferences preferences, Float newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putFloat(key, newValue).apply();
            }
        }
    }

    private static class BooleanUpdater implements Preferences.Pref.Updater<Boolean> {
        private final String key;

        BooleanUpdater(String key) {
            this.key = key;
        }

        @Override
        public void update(SharedPreferences preferences, Boolean newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putBoolean(key, newValue).apply();
            }
        }
    }

    private static class StringUpdater implements Preferences.Pref.Updater<String> {
        private final String key;

        StringUpdater(String key) {
            this.key = key;
        }

        @Override
        public void update(SharedPreferences preferences, String newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putString(key, newValue).apply();
            }
        }
    }

    private Prefs() {
        // no instance
    }
}
