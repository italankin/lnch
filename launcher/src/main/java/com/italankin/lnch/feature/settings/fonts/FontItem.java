package com.italankin.lnch.feature.settings.fonts;

import android.graphics.Typeface;

class FontItem {

    final String name;
    final String previewText;
    final Typeface typeface;
    final boolean isDefault;

    FontItem(String name, String previewText, Typeface typeface, boolean isDefault) {
        this.name = name;
        this.previewText = previewText;
        this.typeface = typeface;
        this.isDefault = isDefault;
    }

    @Override
    public int hashCode() {
        return typeface.hashCode();
    }
}
