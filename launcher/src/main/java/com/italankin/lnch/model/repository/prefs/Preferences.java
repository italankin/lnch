package com.italankin.lnch.model.repository.prefs;

import android.graphics.Typeface;

public interface Preferences {

    boolean searchShowSoftKeyboard();

    HomeLayout homeLayout();

    void setOverlayColor(int color);

    int overlayColor();

    boolean useCustomTabs();

    boolean showScrollbar();

    void setItemTextSize(float size);

    float itemTextSize();

    void setItemPadding(int padding);

    int itemPadding();

    void setItemShadowRadius(float radius);

    float itemShadowRadius();

    void setItemFont(Font font);

    Font itemFont();

    void resetItemSettings();

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    enum HomeLayout {
        COMPACT("compact"),
        GRID("grid"),
        LINEAR("linear");

        static HomeLayout from(String s) {
            for (HomeLayout value : values()) {
                if (value.name.equals(s)) {
                    return value;
                }
            }
            return COMPACT;
        }

        private final String name;

        HomeLayout(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Font {
        DEFAULT("default", Typeface.DEFAULT_BOLD),
        SANS_SERIF("sans_serif", Typeface.SANS_SERIF),
        SERIF("serif", Typeface.SERIF),
        MONOSPACE("monospace", Typeface.MONOSPACE);

        static Font from(String s) {
            for (Font value : values()) {
                if (value.name.equals(s)) {
                    return value;
                }
            }
            return DEFAULT;
        }

        private final String name;
        private final Typeface typeface;

        Font(String name, Typeface typeface) {
            this.name = name;
            this.typeface = typeface;
        }

        public Typeface typeface() {
            return typeface;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
