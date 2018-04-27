package com.italankin.lnch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.italankin.lnch.R;

public class AppPrefs {
    private final Context context;
    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public boolean searchShowSoftKeyboard() {
        return prefs.getBoolean(context.getString(R.string.prefs_search_show_soft_keyboard), true);
    }

    public String homeLayout() {
        return prefs.getString(context.getString(R.string.prefs_home_layout), null);
    }
}
