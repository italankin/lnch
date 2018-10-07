package com.italankin.lnch.model.repository.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.italankin.lnch.R;

import javax.inject.Inject;

public class UserPreferences implements Preferences {
    private final Context context;
    private final SharedPreferences prefs;

    @Inject
    public UserPreferences(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    @Override
    public boolean searchShowSoftKeyboard() {
        return prefs.getBoolean(context.getString(R.string.pref_search_show_soft_keyboard), true);
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
}
