package com.italankin.lnch.model.repository.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import io.reactivex.Observable;

public class UserPreferences implements Preferences {

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
    public ColorTheme colorTheme() {
        String pref = prefs.getString(Keys.COLOR_THEME, null);
        return ColorTheme.from(pref);
    }

    @Override
    public boolean searchShowSoftKeyboard() {
        return prefs.getBoolean(Keys.SEARCH_SHOW_SOFT_KEYBOARD, true);
    }

    @Override
    public boolean searchShowGlobal() {
        return prefs.getBoolean(Keys.SEARCH_SHOW_GLOBAL_SEARCH, true);
    }

    @Override
    public boolean scrollToTop() {
        return prefs.getBoolean(Keys.SCROLL_TO_TOP, true);
    }

    @Override
    public HomeLayout homeLayout() {
        String pref = prefs.getString(Keys.HOME_LAYOUT, null);
        return HomeLayout.from(pref);
    }

    @Override
    public void setOverlayColor(int color) {
        prefs.edit().putInt(Keys.WALLPAPER_OVERLAY_COLOR, color).apply();
    }

    @Override
    public int overlayColor() {
        boolean show = prefs.getBoolean(Keys.WALLPAPER_OVERLAY_SHOW, false);
        int defValue = Color.TRANSPARENT;
        return show
                ? prefs.getInt(Keys.WALLPAPER_OVERLAY_COLOR, defValue)
                : defValue;
    }

    @Override
    public boolean useCustomTabs() {
        return prefs.getBoolean(Keys.SEARCH_USE_CUSTOM_TABS, true);
    }

    @Override
    public boolean showScrollbar() {
        return prefs.getBoolean(Keys.SHOW_SCROLLBAR, false);
    }

    @Override
    public EnumSet<SearchTarget> searchTargets() {
        Set<String> set = prefs.getStringSet(Keys.SEARCH_TARGETS, null);
        if (set == null) {
            return SearchTarget.ALL;
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

    @Override
    public void setItemTextSize(float size) {
        prefs.edit().putFloat(Keys.ITEM_TEXT_SIZE, size).apply();
    }

    @Override
    public float itemTextSize() {
        return prefs.getFloat(Keys.ITEM_TEXT_SIZE, Defaults.ITEM_TEXT_SIZE);
    }

    @Override
    public void setItemPadding(int padding) {
        prefs.edit().putInt(Keys.ITEM_PADDING, padding).apply();
    }

    @Override
    public int itemPadding() {
        return prefs.getInt(Keys.ITEM_PADDING, Defaults.ITEM_PADDING);
    }

    @Override
    public void setItemShadowRadius(float radius) {
        prefs.edit().putFloat(Keys.ITEM_SHADOW_RADIUS, radius).apply();
    }

    @Override
    public float itemShadowRadius() {
        return prefs.getFloat(Keys.ITEM_SHADOW_RADIUS, Defaults.ITEM_SHADOW_RADIUS);
    }

    @Override
    public void setItemShadowColor(int color) {
        prefs.edit().putInt(Keys.ITEM_SHADOW_COLOR, color).apply();
    }

    @Nullable
    @Override
    public Integer itemShadowColor() {
        String key = Keys.ITEM_SHADOW_COLOR;
        return prefs.contains(key) ? prefs.getInt(key, 0) : null;
    }

    @Override
    public void setItemFont(Font font) {
        prefs.edit().putString(Keys.ITEM_FONT, font.toString()).apply();
    }

    @Override
    public Font itemFont() {
        String pref = prefs.getString(Keys.ITEM_FONT, null);
        return Font.from(pref);
    }

    @Override
    public void resetItemSettings() {
        prefs.edit()
                .remove(Keys.ITEM_TEXT_SIZE)
                .remove(Keys.ITEM_PADDING)
                .remove(Keys.ITEM_SHADOW_RADIUS)
                .remove(Keys.ITEM_FONT)
                .apply();
    }

    @Override
    public String searchEngine() {
        return prefs.getString(Keys.SEARCH_ENGINE, null);
    }

    @Override
    public LongClickAction appLongClickAction() {
        String pref = prefs.getString(Keys.APP_LONG_CLICK_ACTION, null);
        return LongClickAction.from(pref);
    }

    @Override
    public ScreenOrientation screenOrientation() {
        String pref = prefs.getString(Keys.SCREEN_ORIENTATION, null);
        return ScreenOrientation.from(pref);
    }

    @Override
    public boolean firstLaunch() {
        return prefs.getBoolean(Keys.FIRST_LAUNCH, true);
    }

    @Override
    public void setFirstLaunch(boolean value) {
        prefs.edit().putBoolean(Keys.FIRST_LAUNCH, value).apply();
    }

    @Override
    public AppsSortMode appsSortMode() {
        String pref = prefs.getString(Keys.APPS_SORT_MODE, null);
        return AppsSortMode.from(pref);
    }

    @Override
    public Observable<String> observe() {
        return updates;
    }
}
