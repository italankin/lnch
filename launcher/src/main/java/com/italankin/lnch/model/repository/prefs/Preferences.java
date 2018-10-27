package com.italankin.lnch.model.repository.prefs;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;

import java.util.EnumSet;

public interface Preferences {

    boolean searchShowSoftKeyboard();

    boolean searchShowGlobal();

    HomeLayout homeLayout();

    void setOverlayColor(@ColorInt int color);

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

    void setItemShadowColor(@ColorInt int color);

    @ColorInt
    int itemShadowColor();

    void setItemFont(Font font);

    Font itemFont();

    void resetItemSettings();

    interface Constraints {
        int ITEM_TEXT_SIZE_MIN = 12;
        int ITEM_TEXT_SIZE_MAX = 40;
        int ITEM_PADDING_MIN = 4;
        int ITEM_PADDING_MAX = 28;
        int ITEM_SHADOW_RADIUS_MIN = 0;
        int ITEM_SHADOW_RADIUS_MAX = 16;
    }

    interface Defaults {
        int ITEM_PADDING = 16;
        float ITEM_TEXT_SIZE = 22;
        float ITEM_SHADOW_RADIUS = 4;
    }

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
