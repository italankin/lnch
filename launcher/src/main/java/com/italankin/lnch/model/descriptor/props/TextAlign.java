package com.italankin.lnch.model.descriptor.props;

import androidx.annotation.Nullable;

public enum TextAlign {
    START("start"),
    CENTER("center"),
    END("end");

    private final String key;

    TextAlign(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    @Nullable
    public static TextAlign fromKey(@Nullable String key) {
        if (key == null) {
            return null;
        }
        for (TextAlign align : values()) {
            if (align.key.equals(key)) {
                return align;
            }
        }
        return null;
    }
}
