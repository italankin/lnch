package com.italankin.lnch.model.repository.prefs;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;

import java.util.EnumSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;

public interface Preferences {

    ColorTheme colorTheme();

    boolean searchShowSoftKeyboard();

    boolean searchShowGlobal();

    boolean scrollToTop();

    HomeLayout homeLayout();

    void setOverlayColor(@ColorInt int color);

    int overlayColor();

    boolean useCustomTabs();

    boolean showScrollbar();

    String searchEngine();

    EnumSet<SearchTarget> searchTargets();

    void setItemTextSize(float size);

    float itemTextSize();

    void setItemPadding(int padding);

    int itemPadding();

    void setItemShadowRadius(float radius);

    float itemShadowRadius();

    void setItemShadowColor(@ColorInt int color);

    @Nullable
    @ColorInt
    Integer itemShadowColor();

    void setItemFont(Font font);

    Font itemFont();

    void resetItemSettings();

    LongClickAction appLongClickAction();

    ScreenOrientation screenOrientation();

    boolean firstLaunch();

    void setFirstLaunch(boolean value);

    AppsSortMode appsSortMode();

    Observable<String> observe();

    ///////////////////////////////////////////////////////////////////////////
    // Keys
    ///////////////////////////////////////////////////////////////////////////

    interface Keys {
        String SEARCH_SHOW_SOFT_KEYBOARD = "search_show_soft_keyboard";
        String SEARCH_SHOW_GLOBAL_SEARCH = "search_show_global_search";
        String SEARCH_USE_CUSTOM_TABS = "search_use_custom_tabs";
        String SEARCH_ENGINE = "search_engine";
        String SEARCH_TARGETS = "search_targets";
        String WALLPAPER_OVERLAY_SHOW = "wallpaper_overlay_show";
        String WALLPAPER_OVERLAY_COLOR = "wallpaper_overlay_color";
        String HOME_LAYOUT = "home_layout";
        String SHOW_SCROLLBAR = "show_scrollbar";
        String APP_LONG_CLICK_ACTION = "app_long_click_action";
        String SCREEN_ORIENTATION = "screen_orientation";
        String SCROLL_TO_TOP = "scroll_to_top";
        String COLOR_THEME = "color_theme";
        String ITEM_TEXT_SIZE = "item_text_size";
        String ITEM_PADDING = "item_padding";
        String ITEM_SHADOW_RADIUS = "item_shadow_radius";
        String ITEM_SHADOW_COLOR = "item_shadow_color";
        String ITEM_FONT = "item_font";
        String FIRST_LAUNCH = "first_launch";
        String APPS_SORT_MODE = "apps_sort_mode";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

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

    /**
     * Color theme of the Launcher's UI
     */
    enum ColorTheme {
        DARK("dark"),
        LIGHT("light");

        static ColorTheme from(String s) {
            for (ColorTheme value : values()) {
                if (value.key.equals(s)) {
                    return value;
                }
            }
            return DARK;
        }

        private final String key;

        ColorTheme(String key) {
            this.key = key;
        }
    }

    /**
     * Layout for home screen
     */
    enum HomeLayout {
        COMPACT("compact");

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

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Font for item labels
     */
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

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Search items of these properties
     */
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

    /**
     * Action on item long click
     */
    enum LongClickAction {
        POPUP("popup"),
        INFO("info");

        static LongClickAction from(String s) {
            for (LongClickAction item : values()) {
                if (item.action.equals(s)) {
                    return item;
                }
            }
            return POPUP;
        }

        private final String action;

        LongClickAction(String action) {
            this.action = action;
        }

        @NonNull
        @Override
        public String toString() {
            return action;
        }
    }

    /**
     * Launcher screen orientation
     */
    enum ScreenOrientation {
        SENSOR("sensor", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
        PORTRAIT("portrait", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
        LANDSCAPE("landscape", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        static ScreenOrientation from(String s) {
            for (ScreenOrientation item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return SENSOR;
        }

        private final String key;
        private final int value;

        ScreenOrientation(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public int value() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Sorting mode for items on home screen
     */
    enum AppsSortMode {
        MANUAL("manual"),
        AZ("az"),
        ZA("za");

        static AppsSortMode from(String s) {
            for (AppsSortMode item : values()) {
                if (item.mode.equals(s)) {
                    return item;
                }
            }
            return MANUAL;
        }

        private final String mode;

        AppsSortMode(String mode) {
            this.mode = mode;
        }

        @NonNull
        @Override
        public String toString() {
            return mode;
        }
    }
}
