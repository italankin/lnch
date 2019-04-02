package com.italankin.lnch.model.repository.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import io.reactivex.Observable;

@SuppressWarnings("unchecked")
public class UserPreferences implements Preferences {

    private final Map<Pref<?>, Fetcher> FETCHERS = new HashMap<>(32);
    private final Map<Pref<?>, Updater> UPDATERS = new HashMap<>(32);
    private final Map<String, Pref<?>> PREFS = new HashMap<>(32);

    {
        FETCHERS.put(SEARCH_SHOW_SOFT_KEYBOARD, this::searchShowSoftKeyboard);
        FETCHERS.put(SEARCH_SHOW_GLOBAL_SEARCH, this::searchShowGlobal);
        FETCHERS.put(SEARCH_USE_CUSTOM_TABS, this::useCustomTabs);
        FETCHERS.put(SEARCH_ENGINE, this::searchEngine);
        FETCHERS.put(CUSTOM_SEARCH_ENGINE_FORMAT, this::customSearchEngineFormat);
        FETCHERS.put(SEARCH_TARGETS, this::searchTargets);
        FETCHERS.put(LARGE_SEARCH_BAR, this::isLargeSearchBar);
        FETCHERS.put(WALLPAPER_OVERLAY_SHOW, this::showWallpaperOverlayColor);
        FETCHERS.put(WALLPAPER_OVERLAY_COLOR, this::wallpaperOverlayColor);
        FETCHERS.put(HOME_LAYOUT, this::homeLayout);
        FETCHERS.put(HOME_ALIGNMENT, this::homeAlignment);
        FETCHERS.put(SHOW_SCROLLBAR, this::showScrollbar);
        FETCHERS.put(APP_LONG_CLICK_ACTION, this::appLongClickAction);
        FETCHERS.put(SCREEN_ORIENTATION, this::screenOrientation);
        FETCHERS.put(SCROLL_TO_TOP, this::scrollToTop);
        FETCHERS.put(COLOR_THEME, this::colorTheme);
        FETCHERS.put(ITEM_TEXT_SIZE, this::itemTextSize);
        FETCHERS.put(ITEM_PADDING, this::itemPadding);
        FETCHERS.put(ITEM_SHADOW_RADIUS, this::itemShadowRadius);
        FETCHERS.put(ITEM_SHADOW_COLOR, this::itemShadowColor);
        FETCHERS.put(ITEM_FONT, this::itemFont);
        FETCHERS.put(FIRST_LAUNCH, this::firstLaunch);
        FETCHERS.put(APPS_SORT_MODE, this::appsSortMode);
        FETCHERS.put(APPS_COLOR_OVERLAY_SHOW, this::appsColorOverlayShow);
        FETCHERS.put(APPS_COLOR_OVERLAY, this::appsColorOverlay);

        UPDATERS.put(SEARCH_SHOW_SOFT_KEYBOARD, newValue -> {
            setSearchShowSoftKeyboard((Boolean) newValue);
        });
        UPDATERS.put(SEARCH_SHOW_GLOBAL_SEARCH, newValue -> {
            setSearchShowGlobalSearch((Boolean) newValue);
        });
        UPDATERS.put(SEARCH_USE_CUSTOM_TABS, newValue -> {
            setSearchUseCustomTabs((Boolean) newValue);
        });
        UPDATERS.put(SEARCH_ENGINE, newValue -> {
            setSearchEngine((SearchEngine) newValue);
        });
        UPDATERS.put(CUSTOM_SEARCH_ENGINE_FORMAT, newValue -> {
            setCustomSearchEngineFormat((String) newValue);
        });
        UPDATERS.put(SEARCH_TARGETS, newValue -> {
            setSearchTargets((EnumSet<SearchTarget>) newValue);
        });
        UPDATERS.put(LARGE_SEARCH_BAR, newValue -> {
            setLargeSearchBar((Boolean) newValue);
        });
        UPDATERS.put(WALLPAPER_OVERLAY_SHOW, newValue -> {
            setWallpaperOverlayShow((Boolean) newValue);
        });
        UPDATERS.put(WALLPAPER_OVERLAY_COLOR, newValue -> {
            setOverlayColor((Integer) newValue);
        });
        UPDATERS.put(HOME_LAYOUT, newValue -> {
            setHomeLayout((HomeLayout) newValue);
        });
        UPDATERS.put(HOME_ALIGNMENT, newValue -> {
            setHomeAlignment((HomeAlignment) newValue);
        });
        UPDATERS.put(SHOW_SCROLLBAR, newValue -> {
            setShowScrollbar((Boolean) newValue);
        });
        UPDATERS.put(APP_LONG_CLICK_ACTION, newValue -> {
            setAppLongClickAction((LongClickAction) newValue);
        });
        UPDATERS.put(SCREEN_ORIENTATION, newValue -> {
            setScreenOrientation((ScreenOrientation) newValue);
        });
        UPDATERS.put(SCROLL_TO_TOP, newValue -> {
            setScrollToTop((Boolean) newValue);
        });
        UPDATERS.put(COLOR_THEME, newValue -> {
            setColorTheme((ColorTheme) newValue);
        });
        UPDATERS.put(ITEM_TEXT_SIZE, newValue -> {
            setItemTextSize((Float) newValue);
        });
        UPDATERS.put(ITEM_PADDING, newValue -> {
            setItemPadding((Integer) newValue);
        });
        UPDATERS.put(ITEM_SHADOW_RADIUS, newValue -> {
            setItemShadowRadius((Float) newValue);
        });
        UPDATERS.put(ITEM_SHADOW_COLOR, newValue -> {
            setItemShadowColor((Integer) newValue);
        });
        UPDATERS.put(ITEM_FONT, newValue -> {
            setItemFont((Font) newValue);
        });
        UPDATERS.put(FIRST_LAUNCH, newValue -> {
            setFirstLaunch((Boolean) newValue);
        });
        UPDATERS.put(APPS_SORT_MODE, newValue -> {
            setAppsSortMode((AppsSortMode) newValue);
        });
        UPDATERS.put(APPS_COLOR_OVERLAY_SHOW, newValue -> {
            setAppsColorOverlayShow((Boolean) newValue);
        });
        UPDATERS.put(APPS_COLOR_OVERLAY, newValue -> {
            setAppsColorOverlay((Integer) newValue);
        });

        for (Pref<?> pref : FETCHERS.keySet()) {
            PREFS.put(pref.key(), pref);
        }
    }

    private final SharedPreferences prefs;
    private final Observable<String> updates;

    @Inject
    public UserPreferences(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.updates = Observable
                .<String>create(emitter -> {
                    OnSharedPreferenceChangeListener listener = (sp, key) -> {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(key);
                        }
                    };
                    prefs.registerOnSharedPreferenceChangeListener(listener);
                    emitter.setCancellable(() -> prefs.unregisterOnSharedPreferenceChangeListener(listener));
                })
                .debounce(100, TimeUnit.MILLISECONDS)
                .share();
    }

    @Override
    public <T> T get(Pref<T> pref) {
        Fetcher fetcher = FETCHERS.get(pref);
        if (fetcher == null) {
            throw new IllegalArgumentException("Unknown pref:" + pref);
        }
        return (T) fetcher.getValue();
    }

    @Override
    public <T> void set(Pref<T> pref, T newValue) {
        Updater updater = UPDATERS.get(pref);
        if (updater == null) {
            throw new IllegalArgumentException("Unknown pref:" + pref);
        }
        updater.setValue(newValue);
    }

    @Override
    public void reset(Pref<?>... prefs) {
        SharedPreferences.Editor editor = this.prefs.edit();
        for (Pref<?> pref : prefs) {
            editor.remove(pref.key());
        }
        editor.apply();
    }

    @Override
    public Observable<Pref<?>> observe() {
        return updates.map(PREFS::get);
    }

    @Override
    public <T> Observable<T> observe(Pref<T> pref) {
        return updates
                .filter(pref.key()::equals)
                .map(key -> get(pref));
    }

    private void setColorTheme(ColorTheme colorTheme) {
        prefs.edit().putString(COLOR_THEME.key(), colorTheme.toString()).apply();
    }

    private ColorTheme colorTheme() {
        String pref = prefs.getString(COLOR_THEME.key(), null);
        return ColorTheme.from(pref, COLOR_THEME.defaultValue());
    }

    private void setSearchShowSoftKeyboard(Boolean newValue) {
        prefs.edit().putBoolean(SEARCH_SHOW_SOFT_KEYBOARD.key(), newValue).apply();
    }

    private void setSearchShowGlobalSearch(Boolean newValue) {
        prefs.edit().putBoolean(SEARCH_SHOW_GLOBAL_SEARCH.key(), newValue).apply();
    }

    private boolean searchShowSoftKeyboard() {
        return prefs.getBoolean(SEARCH_SHOW_SOFT_KEYBOARD.key(), SEARCH_SHOW_SOFT_KEYBOARD.defaultValue());
    }

    private boolean searchShowGlobal() {
        return prefs.getBoolean(SEARCH_SHOW_GLOBAL_SEARCH.key(), SEARCH_SHOW_GLOBAL_SEARCH.defaultValue());
    }

    private void setScrollToTop(Boolean newValue) {
        prefs.edit().putBoolean(SCROLL_TO_TOP.key(), newValue).apply();
    }

    private boolean scrollToTop() {
        return prefs.getBoolean(SCROLL_TO_TOP.key(), SCROLL_TO_TOP.defaultValue());
    }

    private void setHomeLayout(HomeLayout newValue) {
        prefs.edit().putString(HOME_LAYOUT.key(), newValue.toString()).apply();
    }

    private HomeLayout homeLayout() {
        String pref = prefs.getString(HOME_LAYOUT.key(), null);
        return HomeLayout.from(pref, HOME_LAYOUT.defaultValue());
    }

    private void setHomeAlignment(HomeAlignment newValue) {
        prefs.edit().putString(HOME_ALIGNMENT.key(), newValue.toString()).apply();
    }

    private HomeAlignment homeAlignment() {
        String pref = prefs.getString(HOME_ALIGNMENT.key(), null);
        return HomeAlignment.from(pref, HOME_ALIGNMENT.defaultValue());
    }

    private void setOverlayColor(int color) {
        prefs.edit().putInt(WALLPAPER_OVERLAY_COLOR.key(), color).apply();
    }

    private void setWallpaperOverlayShow(Boolean newValue) {
        prefs.edit().putBoolean(WALLPAPER_OVERLAY_SHOW.key(), newValue).apply();
    }

    private boolean showWallpaperOverlayColor() {
        return prefs.getBoolean(WALLPAPER_OVERLAY_SHOW.key(), WALLPAPER_OVERLAY_SHOW.defaultValue());
    }

    private int wallpaperOverlayColor() {
        boolean show = prefs.getBoolean(WALLPAPER_OVERLAY_SHOW.key(), WALLPAPER_OVERLAY_SHOW.defaultValue());
        int defValue = Color.TRANSPARENT;
        return show
                ? prefs.getInt(WALLPAPER_OVERLAY_COLOR.key(), defValue)
                : defValue;
    }

    private void setSearchUseCustomTabs(Boolean newValue) {
        prefs.edit().putBoolean(SEARCH_USE_CUSTOM_TABS.key(), newValue).apply();
    }

    private boolean useCustomTabs() {
        return prefs.getBoolean(SEARCH_USE_CUSTOM_TABS.key(), SEARCH_USE_CUSTOM_TABS.defaultValue());
    }

    private void setShowScrollbar(Boolean newValue) {
        prefs.edit().putBoolean(SHOW_SCROLLBAR.key(), newValue).apply();
    }

    private boolean showScrollbar() {
        return prefs.getBoolean(SHOW_SCROLLBAR.key(), SHOW_SCROLLBAR.defaultValue());
    }

    private void setSearchTargets(EnumSet<SearchTarget> newValue) {
        Set<String> value = new HashSet<>(newValue.size());
        for (SearchTarget searchTarget : newValue) {
            value.add(searchTarget.toString());
        }
        prefs.edit().putStringSet(SEARCH_TARGETS.key(), value).apply();
    }

    private EnumSet<SearchTarget> searchTargets() {
        Set<String> set = prefs.getStringSet(SEARCH_TARGETS.key(), null);
        if (set == null) {
            return SEARCH_TARGETS.defaultValue();
        }
        Set<SearchTarget> result = new HashSet<>();
        for (String s : set) {
            SearchTarget target = SearchTarget.from(s);
            if (target != null) {
                result.add(target);
            }
        }
        return EnumSet.copyOf(result);
    }

    private void setLargeSearchBar(boolean value) {
        prefs.edit().putBoolean(LARGE_SEARCH_BAR.key(), value).apply();
    }

    private boolean isLargeSearchBar() {
        return prefs.getBoolean(LARGE_SEARCH_BAR.key(), LARGE_SEARCH_BAR.defaultValue());
    }

    private void setItemTextSize(float size) {
        prefs.edit().putFloat(ITEM_TEXT_SIZE.key(), size).apply();
    }

    private float itemTextSize() {
        return prefs.getFloat(ITEM_TEXT_SIZE.key(), ITEM_TEXT_SIZE.defaultValue());
    }

    private void setItemPadding(int padding) {
        prefs.edit().putInt(ITEM_PADDING.key(), padding).apply();
    }

    private int itemPadding() {
        return prefs.getInt(ITEM_PADDING.key(), ITEM_PADDING.defaultValue());
    }

    private void setItemShadowRadius(float radius) {
        prefs.edit().putFloat(ITEM_SHADOW_RADIUS.key(), radius).apply();
    }

    private float itemShadowRadius() {
        return prefs.getFloat(ITEM_SHADOW_RADIUS.key(), ITEM_SHADOW_RADIUS.defaultValue());
    }

    private void setItemShadowColor(int color) {
        prefs.edit().putInt(ITEM_SHADOW_COLOR.key(), color).apply();
    }

    @Nullable
    private Integer itemShadowColor() {
        String key = ITEM_SHADOW_COLOR.key();
        return prefs.contains(key) ? prefs.getInt(key, 0) : ITEM_SHADOW_COLOR.defaultValue();
    }

    private void setItemFont(Font font) {
        prefs.edit().putString(ITEM_FONT.key(), font.toString()).apply();
    }

    private Font itemFont() {
        String pref = prefs.getString(ITEM_FONT.key(), null);
        return Font.from(pref, ITEM_FONT.defaultValue());
    }

    private void setSearchEngine(SearchEngine newValue) {
        prefs.edit().putString(SEARCH_ENGINE.key(), newValue.toString()).apply();
    }

    private SearchEngine searchEngine() {
        String pref = prefs.getString(SEARCH_ENGINE.key(), null);
        return SearchEngine.from(pref, SEARCH_ENGINE.defaultValue());
    }

    private void setCustomSearchEngineFormat(String newValue) {
        prefs.edit().putString(CUSTOM_SEARCH_ENGINE_FORMAT.key(), newValue).apply();
    }

    private String customSearchEngineFormat() {
        return prefs.getString(CUSTOM_SEARCH_ENGINE_FORMAT.key(), null);
    }

    private void setAppLongClickAction(LongClickAction newValue) {
        prefs.edit().putString(APP_LONG_CLICK_ACTION.key(), newValue.toString()).apply();
    }

    private LongClickAction appLongClickAction() {
        String pref = prefs.getString(APP_LONG_CLICK_ACTION.key(), null);
        return LongClickAction.from(pref, APP_LONG_CLICK_ACTION.defaultValue());
    }

    private void setScreenOrientation(ScreenOrientation newValue) {
        prefs.edit().putString(SCREEN_ORIENTATION.key(), newValue.toString()).apply();
    }

    private ScreenOrientation screenOrientation() {
        String pref = prefs.getString(SCREEN_ORIENTATION.key(), null);
        return ScreenOrientation.from(pref, SCREEN_ORIENTATION.defaultValue());
    }

    private boolean firstLaunch() {
        return prefs.getBoolean(FIRST_LAUNCH.key(), FIRST_LAUNCH.defaultValue());
    }

    private void setFirstLaunch(boolean value) {
        prefs.edit().putBoolean(FIRST_LAUNCH.key(), value).apply();
    }

    private void setAppsSortMode(AppsSortMode newValue) {
        prefs.edit().putString(APPS_SORT_MODE.key(), newValue.toString()).apply();
    }

    private AppsSortMode appsSortMode() {
        String pref = prefs.getString(APPS_SORT_MODE.key(), null);
        return AppsSortMode.from(pref, APPS_SORT_MODE.defaultValue());
    }

    private boolean appsColorOverlayShow() {
        return prefs.getBoolean(APPS_COLOR_OVERLAY_SHOW.key(), APPS_COLOR_OVERLAY_SHOW.defaultValue());
    }

    private void setAppsColorOverlayShow(Boolean newValue) {
        prefs.edit().putBoolean(APPS_COLOR_OVERLAY_SHOW.key(), newValue).apply();
    }

    private Integer appsColorOverlay() {
        return prefs.getInt(APPS_COLOR_OVERLAY.key(), APPS_COLOR_OVERLAY.defaultValue());
    }

    private void setAppsColorOverlay(int newValue) {
        prefs.edit().putInt(APPS_COLOR_OVERLAY.key(), newValue).apply();
    }

    private interface Fetcher {
        Object getValue();
    }

    private interface Updater {
        void setValue(Object newValue);
    }
}
