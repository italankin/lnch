package com.italankin.lnch.model.repository.prefs;

import android.graphics.Typeface;

import java.util.EnumSet;

public interface Preferences {

    boolean searchShowSoftKeyboard();

    HomeLayout homeLayout();

    void setOverlayColor(int color);

    int overlayColor();

    boolean useCustomTabs();

    boolean showScrollbar();

    EnumSet<SearchTarget> searchTargets();

    void setItemTextSize(float size);

    float itemTextSize();

    void setItemPadding(int padding);

    int itemPadding();

    void setItemShadowRadius(float radius);

    float itemShadowRadius();

    void setItemShadowColor(int color);

    int itemShadowColor();

    void setItemFont(Font font);

    Font itemFont();

    void resetItemSettings();

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    enum HomeLayout {
        COMPACT("compact"),
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

    enum SearchTarget {
        HIDDEN("hidden"),
        SHORTCUT("shortcut"),
        WEB("web"),
        URL("url");

        static final EnumSet<SearchTarget> ALL = EnumSet.allOf(SearchTarget.class);

        static SearchTarget from(String s) {
            for (SearchTarget target : values()) {
                if (target.key.equals(s)) {
                    return target;
                }
            }
            return null;
        }

        private final String key;

        SearchTarget(String key) {
            this.key = key;
        }
    }
}
