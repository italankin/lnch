package com.italankin.lnch.model.descriptor.props;

import androidx.annotation.Nullable;

public enum ItemWidth {
    WRAP_CONTENT("wrap_content"),
    FILL_ROW("fill_row"),
    FILL_ROW_CONTENT_WIDTH("fill_row_content_width");

    private final String key;

    ItemWidth(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    @Nullable
    public static ItemWidth fromKey(@Nullable String key) {
        if (key == null) {
            return null;
        }
        for (ItemWidth width : values()) {
            if (width.key.equals(key)) {
                return width;
            }
        }
        return null;
    }

}
