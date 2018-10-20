package com.italankin.lnch.model.repository.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SeparatorStateImpl implements SeparatorState {

    private static final String PREFS_NAME = "separators";

    private static final int FLAG_EXPANDED = 1;
    private static final int DEFAULT_FLAGS = FLAG_EXPANDED;

    private final SharedPreferences prefs;

    public SeparatorStateImpl(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void setExanded(String id, boolean expanded) {
        if (expanded) {
            addFlags(id, FLAG_EXPANDED);
        } else {
            removeFlags(id, FLAG_EXPANDED);
        }
    }

    @Override
    public boolean isExpanded(String id) {
        return checkFlags(id, FLAG_EXPANDED);
    }

    @Override
    public void remove(String id) {
        prefs.edit().remove(id).apply();
    }

    private int getFlags(String id) {
        return prefs.getInt(id, DEFAULT_FLAGS);
    }

    private void addFlags(String id, int flags) {
        prefs.edit().putInt(id, getFlags(id) | flags).apply();
    }

    private void removeFlags(String id, int flags) {
        prefs.edit().putInt(id, getFlags(id) & ~flags).apply();
    }

    private boolean checkFlags(String id, int flags) {
        return (getFlags(id) & flags) == flags;
    }
}
