package com.italankin.lnch.model.repository.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.italankin.lnch.R;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UserPreferences implements Preferences {

    private final Context context;
    private final SharedPreferences prefs;
    private final Observable<String> updates;

    @Inject
    public UserPreferences(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
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
        return ColorTheme.DARK;
    }

    @Override
    public boolean searchShowSoftKeyboard() {
        return prefs.getBoolean(context.getString(R.string.pref_search_show_soft_keyboard), true);
    }

    @Override
    public boolean searchShowGlobal() {
        return prefs.getBoolean(context.getString(R.string.pref_search_show_global_search), true);
    }

    @Override
    public boolean scrollToTop() {
        return prefs.getBoolean(context.getString(R.string.pref_misc_scroll_to_top), true);
    }

    @Override
    public HomeLayout homeLayout() {
        String pref = prefs.getString(context.getString(R.string.pref_home_layout), null);
        return HomeLayout.from(pref);
    }

    @Override
    public void setOverlayColor(int color) {
        prefs.edit().putInt(context.getString(R.string.pref_wallpaper_overlay_color), color).apply();
    }

    @Override
    public int overlayColor() {
        boolean show = prefs.getBoolean(
                context.getString(R.string.pref_wallpaper_overlay_show), false);
        int defValue = Color.TRANSPARENT;
        return show
                ? prefs.getInt(context.getString(R.string.pref_wallpaper_overlay_color), defValue)
                : defValue;
    }

    @Override
    public boolean useCustomTabs() {
        return prefs.getBoolean(context.getString(R.string.pref_search_use_custom_tabs), true);
    }

    @Override
    public boolean showScrollbar() {
        return prefs.getBoolean(context.getString(R.string.pref_misc_show_scrollbar), false);
    }

    @Override
    public EnumSet<SearchTarget> searchTargets() {
        Set<String> set = prefs.getStringSet(context.getString(R.string.pref_search_targets), null);
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
        prefs.edit().putFloat(context.getString(R.string.pref_item_text_size), size).apply();
    }

    @Override
    public float itemTextSize() {
        return prefs.getFloat(context.getString(R.string.pref_item_text_size), Defaults.ITEM_TEXT_SIZE);
    }

    @Override
    public void setItemPadding(int padding) {
        prefs.edit().putInt(context.getString(R.string.pref_item_padding), padding).apply();
    }

    @Override
    public int itemPadding() {
        return prefs.getInt(context.getString(R.string.pref_item_padding), Defaults.ITEM_PADDING);
    }

    @Override
    public void setItemShadowRadius(float radius) {
        prefs.edit().putFloat(context.getString(R.string.pref_item_shadow_radius), radius).apply();
    }

    @Override
    public float itemShadowRadius() {
        return prefs.getFloat(context.getString(R.string.pref_item_shadow_radius), Defaults.ITEM_SHADOW_RADIUS);
    }

    @Override
    public void setItemShadowColor(int color) {
        prefs.edit().putInt(context.getString(R.string.pref_item_shadow_color), color).apply();
    }

    @Nullable
    @Override
    public Integer itemShadowColor() {
        String key = context.getString(R.string.pref_item_shadow_color);
        return prefs.contains(key) ? prefs.getInt(key, 0) : null;
    }

    @Override
    public void setItemFont(Font font) {
        prefs.edit().putString(context.getString(R.string.pref_item_font), font.toString()).apply();
    }

    @Override
    public Font itemFont() {
        String pref = prefs.getString(context.getString(R.string.pref_item_font), null);
        return Font.from(pref);
    }

    @Override
    public void resetItemSettings() {
        prefs.edit()
                .remove(context.getString(R.string.pref_item_text_size))
                .remove(context.getString(R.string.pref_item_padding))
                .remove(context.getString(R.string.pref_item_shadow_radius))
                .remove(context.getString(R.string.pref_item_font))
                .apply();
    }

    @Override
    public String searchEngine() {
        return prefs.getString(context.getString(R.string.pref_search_engine), null);
    }

    @Override
    public LongClickAction appLongClickAction() {
        String pref = prefs.getString(context.getString(R.string.pref_misc_app_long_click_action), null);
        return LongClickAction.from(pref);
    }

    @Override
    public ScreenOrientation screenOrientation() {
        String pref = prefs.getString(context.getString(R.string.pref_misc_screen_orientation), null);
        return ScreenOrientation.from(pref);
    }

    @Override
    public Observable<String> observe() {
        return updates;
    }
}
