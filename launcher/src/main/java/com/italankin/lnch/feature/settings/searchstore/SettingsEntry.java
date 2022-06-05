package com.italankin.lnch.feature.settings.searchstore;

import java.io.Serializable;

import androidx.annotation.StringRes;

public interface SettingsEntry {

    Key key();

    @StringRes
    int title();

    @StringRes
    int summary();

    @StringRes
    int category();

    SettingStackBuilder stackBuilder();

    interface Key extends Serializable {
    }

    class StringKey implements Key {
        public final String value;

        StringKey(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StringKey stringKey = (StringKey) o;
            return value.equals(stringKey.value);
        }

        @Override
        public String toString() {
            return "StringKey(" + value + ')';
        }
    }

    class ResourceKey implements Key {
        @StringRes
        public final int value;

        ResourceKey(@StringRes int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ResourceKey that = (ResourceKey) o;
            return value == that.value;
        }

        @Override
        public String toString() {
            return "ResourceKey(" + value + ')';
        }
    }
}
