package com.italankin.lnch.model.repository.prefs;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

/**
 * A base interface for interacting with user preferences
 */
public interface Preferences {

    <T> T get(Pref<T> pref);

    <T> void set(Pref<T> pref, T newValue);

    Observable<Pref<?>> observe();

    <T> Observable<T> observe(Pref<T> pref);

    <T> Observable<Value<T>> observeValue(Pref<T> pref);

    void reset(Pref<?>... prefs);

    ///////////////////////////////////////////////////////////////////////////
    // Preferences interfaces
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Preference value wrapper to be used with RxJava streams
     */
    class Value<T> {

        private final T value;

        Value(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }
    }

    /**
     * Base class for an app preference
     *
     * @param <T> type of the preference
     */
    interface Pref<T> {
        String key();

        Fetcher<T> fetcher();

        Updater<T> updater();

        /**
         * Handler for preference reads
         *
         * @param <T> type of the preference
         */
        interface Fetcher<T> {
            /**
             * Read preference value from {@link SharedPreferences}
             */
            T fetch(SharedPreferences preferences, String key);
        }

        /**
         * Handler for preference writes
         *
         * @param <T> type of the preference
         */
        interface Updater<T> {
            /**
             * Write preference value to {@link SharedPreferences}
             */
            void update(SharedPreferences preferences, String key, T newValue);
        }
    }

    /**
     * A preference with {@link RangePref#min()} and {@link RangePref#max()} value bounds
     *
     * @param <T>type of the preference
     */
    interface RangePref<T> extends Pref<T> {
        T min();

        T max();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Preferences
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Show soft keyboard when search bar appears
     */
    Pref<Boolean> SEARCH_SHOW_SOFT_KEYBOARD = Prefs.createBoolean(
            "search_show_soft_keyboard",
            true);

    /**
     * Show 'global search' button in the search bar
     */
    Pref<Boolean> SEARCH_SHOW_GLOBAL_SEARCH = Prefs.createBoolean(
            "search_show_global_search",
            true);

    /**
     * Use 'custom tabs' for opening external search intents
     */
    Pref<Boolean> SEARCH_USE_CUSTOM_TABS = Prefs.createBoolean(
            "search_use_custom_tabs",
            true);

    /**
     * External search engine
     */
    Pref<SearchEngine> SEARCH_ENGINE = Prefs.create(
            "search_engine",
            SearchEngine.GOOGLE,
            SearchEngine::from);

    /**
     * Custom search engine format string
     */
    Pref<String> CUSTOM_SEARCH_ENGINE_FORMAT = Prefs.createString(
            "custom_search_engine_format",
            null);

    /**
     * Items which will appear in the search results
     */
    Pref<EnumSet<SearchTarget>> SEARCH_TARGETS = Prefs.create(
            "search_targets",
            SearchTarget.ALL,
            (preferences, key) -> {
                Set<String> set = preferences.getStringSet(key, null);
                return set != null ? SearchTarget.fromCollection(set) : null;
            },
            (preferences, key, newValue) -> {
                Set<String> value = new HashSet<>(newValue.size());
                for (SearchTarget searchTarget : newValue) {
                    value.add(searchTarget.toString());
                }
                preferences.edit().putStringSet(key, value).apply();
            });

    /**
     * Increase size for search bar
     */
    Pref<Boolean> LARGE_SEARCH_BAR = Prefs.createBoolean(
            "large_search_bar",
            false);

    /**
     * Show wallpaper color overlay
     */
    Pref<Boolean> WALLPAPER_OVERLAY_SHOW = Prefs.createBoolean(
            "wallpaper_overlay_show",
            false);

    /**
     * Color for a {@link #WALLPAPER_OVERLAY_SHOW}
     */
    Pref<Integer> WALLPAPER_OVERLAY_COLOR = Prefs.createInteger(
            "wallpaper_overlay_color",
            Color.TRANSPARENT);

    /**
     * Layout for the home screen
     */
    Pref<HomeLayout> HOME_LAYOUT = Prefs.create(
            "home_layout",
            HomeLayout.COMPACT,
            HomeLayout::from);

    /**
     * Layout alignment for the home screen
     */
    Pref<HomeAlignment> HOME_ALIGNMENT = Prefs.create(
            "home_alignment",
            HomeAlignment.START,
            HomeAlignment::from);

    /**
     * Show scrollbar on the home screen
     */
    Pref<Boolean> SHOW_SCROLLBAR = Prefs.createBoolean(
            "show_scrollbar",
            false);

    /**
     * Action to perform when long press on home screen item is detected
     */
    Pref<LongClickAction> APP_LONG_CLICK_ACTION = Prefs.create(
            "app_long_click_action",
            LongClickAction.POPUP,
            LongClickAction::from);

    /**
     * Launcher screen orientation
     */
    Pref<ScreenOrientation> SCREEN_ORIENTATION = Prefs.create(
            "screen_orientation",
            ScreenOrientation.SENSOR,
            ScreenOrientation::from);

    /**
     * Scroll home screen to top when pressing 'home' button
     */
    Pref<Boolean> SCROLL_TO_TOP = Prefs.createBoolean(
            "scroll_to_top",
            true);

    /**
     * {@link #SCROLL_TO_TOP} smoothly
     */
    Pref<Boolean> SMOOTH_SCROLL_TO_TOP = Prefs.createBoolean(
            "smooth_scroll_to_top",
            true);

    /**
     * Color theme of the launcher
     */
    Pref<ColorTheme> COLOR_THEME = Prefs.create(
            "color_theme",
            ColorTheme.SYSTEM,
            ColorTheme::from);

    /**
     * Text size of the home screen items
     */
    RangePref<Float> ITEM_TEXT_SIZE = Prefs.createFloatRange(
            "item_text_size",
            22f, 12f, 40f);

    /**
     * Home screen items padding
     */
    RangePref<Integer> ITEM_PADDING = Prefs.createIntegerRange(
            "item_padding",
            16, 4, 28);

    /**
     * Shadow of the home screen items
     */
    RangePref<Float> ITEM_SHADOW_RADIUS = Prefs.createFloatRange(
            "item_shadow_radius",
            4f, 0f, 16f);

    /**
     * Color for {@link #ITEM_SHADOW_RADIUS}
     */
    Pref<Integer> ITEM_SHADOW_COLOR = Prefs.createInteger(
            "item_shadow_color",
            null);

    /**
     * Home screen items font
     */
    Pref<Font> ITEM_FONT = Prefs.create(
            "item_font",
            Font.DEFAULT,
            Font::from);

    /**
     * First time launch of the launcher
     */
    Pref<Boolean> FIRST_LAUNCH = Prefs.createBoolean(
            "first_launch",
            true);

    /**
     * Home screen items sorting mode
     */
    Pref<AppsSortMode> APPS_SORT_MODE = Prefs.create(
            "apps_sort_mode",
            AppsSortMode.MANUAL,
            AppsSortMode::from);

    /**
     * Single color overlay for all apps
     */
    Pref<Boolean> APPS_COLOR_OVERLAY_SHOW = Prefs.createBoolean(
            "apps_color_overlay_show",
            false);

    /**
     * Color for {@link #APPS_COLOR_OVERLAY_SHOW}
     */
    Pref<Integer> APPS_COLOR_OVERLAY = Prefs.createInteger(
            "apps_color_overlay",
            Color.WHITE);

    /**
     * Expand notifications pane on home screen overscroll
     */
    Pref<Boolean> EXPAND_NOTIFICATIONS = Prefs.createBoolean(
            "expand_notifications",
            true);

    /**
     * Color of the status bar on the home screen
     */
    Pref<Integer> STATUS_BAR_COLOR = Prefs.createInteger(
            "status_bar_color",
            null);

    /**
     * Maximum app dynamic shortcuts to display
     */
    Pref<String> MAX_DYNAMIC_SHORTCUTS = Prefs.createString(
            "max_dynamic_shortcuts",
            "default");

    /**
     * Enable widgets page
     */
    Pref<Boolean> ENABLE_WIDGETS = Prefs.createBoolean(
            "enable_widgets",
            false);

    /**
     * Widgets page position
     */
    Pref<WidgetsPosition> WIDGETS_POSITION = Prefs.create(
            "widgets_position",
            WidgetsPosition.LEFT,
            WidgetsPosition::from);

    /**
     * Animate home screen list appearance
     */
    Pref<Boolean> APPS_LIST_ANIMATE = Prefs.createBoolean(
            "apps_list_animate",
            true);

    /**
     * App shortcuts sort mode
     */
    Pref<ShortcutsSortMode> SHORTCUTS_SORT_MODE = Prefs.create(
            "shortcuts_sort_mode",
            ShortcutsSortMode.DEFAULT,
            ShortcutsSortMode::from);

    /**
     * Enable notification dots for the apps
     */
    Pref<Boolean> NOTIFICATION_DOT = Prefs.createBoolean(
            "notification_dot",
            false);

    /**
     * Color of {@link #NOTIFICATION_DOT}
     */
    Pref<Integer> NOTIFICATION_DOT_COLOR = Prefs.createInteger(
            "notification_dot_color",
            null);

    /**
     * Show {@link #NOTIFICATION_DOT} for ongoing notifications
     */
    Pref<Boolean> NOTIFICATION_DOT_ONGOING = Prefs.createBoolean(
            "notification_dot_ongoing",
            false);

    /**
     * Enable notification dots for the apps
     */
    Pref<Boolean> NOTIFICATION_POPUP = Prefs.createBoolean(
            "notification_popup",
            false);

    /**
     * Creating and editing intents via {@link com.italankin.lnch.feature.intentfactory.IntentFactoryActivity}
     */
    Pref<Boolean> EXPERIMENTAL_INTENT_FACTORY = Prefs.createBoolean(
            "experimental_intent_factory",
            false);

    /**
     * Creating and editing intents via {@link com.italankin.lnch.feature.intentfactory.IntentFactoryActivity}
     */
    Pref<Boolean> HOME_PAGER_INDICATOR = Prefs.createBoolean(
            "home_pager_indicator",
            false);

    /**
     * Show folders in fullscreen mode instead of popup
     */
    Pref<Boolean> FULLSCREEN_FOLDERS = Prefs.createBoolean(
            "fullscreen_folders",
            false);

    List<Pref<?>> ALL = Arrays.asList(
            SEARCH_SHOW_SOFT_KEYBOARD,
            SEARCH_SHOW_GLOBAL_SEARCH,
            SEARCH_USE_CUSTOM_TABS,
            SEARCH_ENGINE,
            CUSTOM_SEARCH_ENGINE_FORMAT,
            SEARCH_TARGETS,
            LARGE_SEARCH_BAR,
            WALLPAPER_OVERLAY_SHOW,
            WALLPAPER_OVERLAY_COLOR,
            HOME_LAYOUT,
            HOME_ALIGNMENT,
            SHOW_SCROLLBAR,
            APP_LONG_CLICK_ACTION,
            SCREEN_ORIENTATION,
            SCROLL_TO_TOP,
            SMOOTH_SCROLL_TO_TOP,
            COLOR_THEME,
            ITEM_TEXT_SIZE,
            ITEM_PADDING,
            ITEM_SHADOW_RADIUS,
            ITEM_SHADOW_COLOR,
            ITEM_FONT,
            FIRST_LAUNCH,
            APPS_SORT_MODE,
            APPS_COLOR_OVERLAY_SHOW,
            APPS_COLOR_OVERLAY,
            EXPAND_NOTIFICATIONS,
            STATUS_BAR_COLOR,
            MAX_DYNAMIC_SHORTCUTS,
            STATUS_BAR_COLOR,
            ENABLE_WIDGETS,
            WIDGETS_POSITION,
            APPS_LIST_ANIMATE,
            SHORTCUTS_SORT_MODE,
            NOTIFICATION_DOT,
            NOTIFICATION_DOT_COLOR,
            NOTIFICATION_DOT_ONGOING,
            NOTIFICATION_POPUP,
            EXPERIMENTAL_INTENT_FACTORY,
            HOME_PAGER_INDICATOR,
            FULLSCREEN_FOLDERS
    );

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Color theme of the Launcher's UI
     */
    enum ColorTheme {
        SYSTEM("system"),
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
     * Layout alignment for home screen
     */
    enum HomeAlignment {
        START("start"),
        CENTER("center"),
        END("end");

        static HomeAlignment from(String s, HomeAlignment defaultValue) {
            for (HomeAlignment value : values()) {
                if (value.key.equals(s)) {
                    return value;
                }
            }
            return defaultValue;
        }

        private final String key;

        HomeAlignment(String key) {
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
     * Search properties
     */
    enum SearchTarget {
        IGNORED("ignored"),
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

        static EnumSet<SearchTarget> fromCollection(Collection<? extends String> strings) {
            Set<SearchTarget> result = new HashSet<>();
            for (String s : strings) {
                SearchTarget target = SearchTarget.from(s);
                if (target != null) {
                    result.add(target);
                }
            }
            return EnumSet.copyOf(result);
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

    /**
     * Widgets page position on home screen
     */
    enum WidgetsPosition {
        LEFT("left"),
        RIGHT("right");

        static WidgetsPosition from(String s, WidgetsPosition defaultValue) {
            for (WidgetsPosition item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
        }

        private final String key;

        WidgetsPosition(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * External search engines
     */
    enum SearchEngine {
        GOOGLE("google"),
        BING("bing"),
        YANDEX("yandex"),
        DDG("ddg"),
        BAIDU("baidu"),
        CUSTOM("custom");

        static SearchEngine from(String s, SearchEngine defaultValue) {
            for (SearchEngine item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
        }

        private final String key;

        SearchEngine(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Sorting mode for shortcuts
     */
    enum ShortcutsSortMode {
        DEFAULT("default"),
        REVERSED("reversed");

        static ShortcutsSortMode from(String s, ShortcutsSortMode defaultValue) {
            for (ShortcutsSortMode item : values()) {
                if (item.key.equals(s)) {
                    return item;
                }
            }
            return defaultValue;
        }

        private final String key;

        ShortcutsSortMode(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }
    }
}
