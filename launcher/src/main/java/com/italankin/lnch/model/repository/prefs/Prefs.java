package com.italankin.lnch.model.repository.prefs;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Factory for {@link com.italankin.lnch.model.repository.prefs.Preferences.Pref}
 */
final class Prefs {

    static Preferences.Pref<Integer> createInteger(String key, Integer defaultValue) {
        return new BasePref<>(key, Integer.class, defaultValue, new IntegerUpdater());
    }

    static Preferences.Pref<Boolean> createBoolean(String key, Boolean defaultValue) {
        return new BasePref<>(key, Boolean.class, defaultValue, new BooleanUpdater());
    }

    static Preferences.Pref<String> createString(String key, String defaultValue) {
        return new BasePref<>(key, String.class, defaultValue, new StringUpdater());
    }

    static Preferences.RangePref<Float> createFloatRange(String key, Float defaultValue, Float min, Float max) {
        return new RangePref<>(key, Float.class, defaultValue, min, max, new FloatRangeUpdater(min, max));
    }

    static Preferences.RangePref<Integer> createIntegerRange(String key, Integer defaultValue, Integer min, Integer max) {
        return new RangePref<>(key, Integer.class, defaultValue, min, max, new IntegerRangeUpdater(min, max));
    }

    static <T> Preferences.Pref<T> create(
            String prefKey,
            Class<?> type,
            T defaultValue,
            Preferences.Pref.Fetcher<T> fetcher,
            Preferences.Pref.Updater<T> updater
    ) {
        return new BasePref<T>(
                prefKey,
                type,
                defaultValue,
                (preferences, key) -> {
                    T value = fetcher.fetch(preferences, key);
                    return value != null ? value : defaultValue;
                },
                updater
        );
    }

    static <T> Preferences.Pref<T> create(String prefKey, T defaultValue, ValueConverter<T> valueConverter) {
        return new BasePref<T>(
                prefKey,
                String.class,
                defaultValue,
                (preferences, key) -> {
                    String value = preferences.getString(key, null);
                    return valueConverter.convert(value, defaultValue);
                },
                new ObjectUpdater<>()
        );
    }

    interface ValueConverter<T> {
        T convert(@Nullable String value, T defaultValue);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Impl
    ///////////////////////////////////////////////////////////////////////////

    private static class BasePref<T> implements Preferences.Pref<T> {
        private final String key;
        private final T defaultValue;
        private final Fetcher<T> fetcher;
        private final Updater<T> updater;
        private final Class<?> type;

        BasePref(String key, Class<T> type, T defaultValue, Updater<T> updater) {
            this(key, type, defaultValue, new ObjectFetcher<>(key, defaultValue), updater);
        }

        BasePref(String key, Class<?> type, T defaultValue, Fetcher<T> fetcher, Updater<T> updater) {
            this.key = key;
            this.type = type;
            this.defaultValue = defaultValue;
            this.fetcher = fetcher;
            this.updater = updater;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Class<?> valueType() {
            return type;
        }

        @Override
        public T defaultValue() {
            return defaultValue;
        }

        @Override
        public Fetcher<T> fetcher() {
            return fetcher;
        }

        @Override
        public Updater<T> updater() {
            return updater;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Preferences.Pref) {
                return key.equals(((Preferences.Pref<?>) obj).key());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    private static class RangePref<T> extends BasePref<T> implements Preferences.RangePref<T> {
        private final T min;
        private final T max;

        RangePref(String key, Class<T> type, T defaultValue, T min, T max, Updater<T> updater) {
            super(key, type, defaultValue, updater);
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
        public T fetch(SharedPreferences preferences, String key) {
            Map<String, ?> map = preferences.getAll();
            Object value = map.get(this.key);
            return value == null ? defaultValue : (T) value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Updaters
    ///////////////////////////////////////////////////////////////////////////

    private static class ObjectUpdater<T> implements Preferences.Pref.Updater<T> {
        @Override
        public void update(SharedPreferences preferences, String key, T newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putString(key, newValue.toString()).apply();
            }
        }
    }

    private static class IntegerUpdater implements Preferences.Pref.Updater<Integer> {
        @Override
        public void update(SharedPreferences preferences, String key, Integer newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putInt(key, newValue).apply();
            }
        }
    }

    private static class IntegerRangeUpdater extends IntegerUpdater {
        private final int min;
        private final int max;

        IntegerRangeUpdater(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public void update(SharedPreferences preferences, String key, Integer newValue) {
            if (newValue != null) {
                if (newValue < min) {
                    newValue = min;
                } else if (newValue > max) {
                    newValue = max;
                }
            }
            super.update(preferences, key, newValue);
        }
    }

    private static class FloatUpdater implements Preferences.Pref.Updater<Float> {
        @Override
        public void update(SharedPreferences preferences, String key, Float newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putFloat(key, newValue).apply();
            }
        }
    }

    private static class FloatRangeUpdater extends FloatUpdater {
        private final float min;
        private final float max;

        FloatRangeUpdater(float min, float max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public void update(SharedPreferences preferences, String key, Float newValue) {
            if (newValue != null) {
                if (newValue < min) {
                    newValue = min;
                } else if (newValue > max) {
                    newValue = max;
                }
            }
            super.update(preferences, key, newValue);
        }
    }

    private static class BooleanUpdater implements Preferences.Pref.Updater<Boolean> {
        @Override
        public void update(SharedPreferences preferences, String key, Boolean newValue) {
            if (newValue == null) {
                preferences.edit().remove(key).apply();
            } else {
                preferences.edit().putBoolean(key, newValue).apply();
            }
        }
    }

    private static class StringUpdater implements Preferences.Pref.Updater<String> {
        @Override
        public void update(SharedPreferences preferences, String key, String newValue) {
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
