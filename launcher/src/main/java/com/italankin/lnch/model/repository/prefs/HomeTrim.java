package com.italankin.lnch.model.repository.prefs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Physical left and right empty strips, as percentages of the home screen width.
 */
public final class HomeTrim {

    public static final int MIN_CONTENT_PERCENT = 20;
    public static final HomeTrim NONE = new HomeTrim(0, 0);

    public final int left;
    public final int right;

    public HomeTrim(int left, int right) {
        if (left < 0 || right < 0 || left > 100 - MIN_CONTENT_PERCENT
                || right > 100 - MIN_CONTENT_PERCENT - left) {
            left = 0;
            right = 0;
        }
        this.left = left;
        this.right = right;
    }

    static HomeTrim from(@Nullable String value, HomeTrim defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String[] parts = value.split(",", -1);
        if (parts.length != 2) {
            return defaultValue;
        }
        try {
            return new HomeTrim(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HomeTrim)) {
            return false;
        }
        HomeTrim other = (HomeTrim) obj;
        return left == other.left && right == other.right;
    }

    @Override
    public int hashCode() {
        return 31 * left + right;
    }

    @NonNull
    @Override
    public String toString() {
        return left + "," + right;
    }
}
