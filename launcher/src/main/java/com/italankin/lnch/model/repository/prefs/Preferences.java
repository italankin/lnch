package com.italankin.lnch.model.repository.prefs;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

public interface Preferences {

    <T> T get(Pref<T> pref);

    <T> void set(Pref<T> pref, T newValue);

    Observable<Pref<?>> observe();

    <T> Observable<T> observe(Pref<T> pref);

    void reset(Pref<?>... prefs);

    ///////////////////////////////////////////////////////////////////////////
    // Preferences interfaces
    ///////////////////////////////////////////////////////////////////////////

    interface Pref<T> {
        String key();

        T defaultValue();
    }

    interface RangePref<T> extends Pref<T> {
        T min();

        T max();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Preferences
    ///////////////////////////////////////////////////////////////////////////

    Pref<Boolean> SEARCH_SHOW_SOFT_KEYBOARD = Prefs.create(
            "search_show_soft_keyboard",
            true);

    Pref<Boolean> SEARCH_SHOW_GLOBAL_SEARCH = Prefs.create(
            "search_show_global_search",
            true);

    Pref<Boolean> SEARCH_USE_CUSTOM_TABS = Prefs.create(
            "search_use_custom_tabs",
            true);

    Pref<String> SEARCH_ENGINE = Prefs.create(
            "search_engine",
            "google");

    Pref<EnumSet<SearchTarget>> SEARCH_TARGETS = Prefs.create(
            "search_targets",
            SearchTarget.ALL);

    Pref<Boolean> WALLPAPER_OVERLAY_SHOW = Prefs.create(
            "wallpaper_overlay_show",
            false);

    Pref<Integer> WALLPAPER_OVERLAY_COLOR = Prefs.create(
            "wallpaper_overlay_color",
            Color.TRANSPARENT);

    Pref<HomeLayout> HOME_LAYOUT = Prefs.create(
            "home_layout",
            HomeLayout.COMPACT);

    Pref<Boolean> SHOW_SCROLLBAR = Prefs.create(
            "show_scrollbar",
            false);

    Pref<LongClickAction> APP_LONG_CLICK_ACTION = Prefs.create(
            "app_long_click_action",
            LongClickAction.POPUP);

    Pref<ScreenOrientation> SCREEN_ORIENTATION = Prefs.create(
            "screen_orientation",
            ScreenOrientation.SENSOR);

    Pref<Boolean> SCROLL_TO_TOP = Prefs.create(
            "scroll_to_top",
            true);

    Pref<ColorTheme> COLOR_THEME = Prefs.create(
            "color_theme",
            ColorTheme.DARK);

    RangePref<Float> ITEM_TEXT_SIZE = Prefs.create(
            "item_text_size",
            22f, 12f, 40f);

    RangePref<Integer> ITEM_PADDING = Prefs.create(
            "item_padding",
            16, 4, 28);

    RangePref<Float> ITEM_SHADOW_RADIUS = Prefs.create(
            "item_shadow_radius",
            4f, 0f, 16f);

    Pref<Integer> ITEM_SHADOW_COLOR = Prefs.create(
            "item_shadow_color",
            null);

    Pref<Font> ITEM_FONT = Prefs.create(
            "item_font",
            Font.DEFAULT);

    Pref<Boolean> FIRST_LAUNCH = Prefs.create(
            "first_launch",
            true);

    Pref<AppsSortMode> APPS_SORT_MODE = Prefs.create(
            "apps_sort_mode",
            AppsSortMode.MANUAL);

    Pref<Boolean> APPS_COLOR_OVERLAY_SHOW = Prefs.create(
            "apps_color_overlay_show",
            false);

    Pref<Integer> APPS_COLOR_OVERLAY = Prefs.create(
            "apps_color_overlay",
            Color.WHITE);

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Color theme of the Launcher's UI
     */
    enum ColorTheme {
        DARK("dark"),
        LIGHT("light");

        static ColorTheme from(String s, ColorTheme defaultValue) {
            for (ColorTheme value : values()) {
                if (value.key.equals(s)) {
                    return value;
                }
            }
            return defaultValue;
        }

        private final String key;

        ColorTheme(String key) {
            this.key = key;
        }

        @NotNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Layout for home screen
     */
    enum HomeLayout {
        COMPACT("compact");

        static HomeLayout from(String s, HomeLayout defaultValue) {
            for (HomeLayout value : values()) {
                if (value.key.equals(s)) {
                    return value;
                }
            }
            return defaultValue;
        }

        private final String key;

        HomeLayout(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
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

        static Font from(String s, Font defaultValue) {
            for (Font value : values()) {
                if (value.key.equals(s)) {
                    return value;
                }
            }
            return defaultValue;
        }

        private final String key;
        private final Typeface typeface;

        Font(String key, Typeface typeface) {
            this.key = key;
            this.typeface = typeface;
        }

        public Typeface typeface() {
            return typeface;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
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

        @NotNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Action on item long click
     */
    enum LongClickAction {
        POPUP("popup"),
        INFO("info");

        static LongClickAction from(String s, LongClickAction defaultValue) {
            for (LongClickAction item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
        }

        private final String key;

        LongClickAction(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Launcher screen orientation
     */
    enum ScreenOrientation {
        SENSOR("sensor", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
        PORTRAIT("portrait", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
        LANDSCAPE("landscape", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        static ScreenOrientation from(String s, ScreenOrientation defaultValue) {
            for (ScreenOrientation item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
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

        static AppsSortMode from(String s, AppsSortMode defaultValue) {
            for (AppsSortMode item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
        }

        private final String key;

        AppsSortMode(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }
}
