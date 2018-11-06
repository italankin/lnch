package com.italankin.lnch.feature.home.model;

import android.support.annotation.ColorInt;

import com.italankin.lnch.model.repository.prefs.Preferences;

public final class UserPrefs {
    public Preferences.HomeLayout homeLayout;
    @ColorInt
    public int overlayColor;
    public boolean showScrollbar;
    public boolean globalSearch;
    public ItemPrefs itemPrefs;

    public UserPrefs(Preferences preferences) {
        homeLayout = preferences.homeLayout();
        overlayColor = preferences.overlayColor();
        showScrollbar = preferences.showScrollbar();
        globalSearch = preferences.searchShowGlobal();
        itemPrefs = new UserPrefs.ItemPrefs(preferences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPrefs userPrefs = (UserPrefs) o;
        if (overlayColor != userPrefs.overlayColor) {
            return false;
        }
        if (showScrollbar != userPrefs.showScrollbar) {
            return false;
        }
        if (globalSearch != userPrefs.globalSearch) {
            return false;
        }
        if (homeLayout != userPrefs.homeLayout) {
            return false;
        }
        return itemPrefs.equals(userPrefs.itemPrefs);
    }

    @Override
    public int hashCode() {
        int result = homeLayout.hashCode();
        result = 31 * result + overlayColor;
        result = 31 * result + (showScrollbar ? 1 : 0);
        result = 31 * result + itemPrefs.hashCode();
        result = 31 * result + (globalSearch ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "homeLayout=" + homeLayout +
                ", overlayColor=" + String.format("#%08x", overlayColor) +
                ", showScrollbar=" + showScrollbar +
                ", globalSearch=" + globalSearch +
                ", itemPrefs=" + itemPrefs +
                '}';
    }

    public static final class ItemPrefs {
        public float itemTextSize;
        public int itemPadding;
        public float itemShadowRadius;
        @ColorInt
        public int itemShadowColor;
        public Preferences.Font itemFont;

        private ItemPrefs(Preferences preferences) {
            itemTextSize = preferences.itemTextSize();
            itemPadding = preferences.itemPadding();
            itemShadowRadius = preferences.itemShadowRadius();
            itemShadowColor = preferences.itemShadowColor();
            itemFont = preferences.itemFont();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ItemPrefs)) {
                return false;
            }
            ItemPrefs itemPrefs = (ItemPrefs) o;
            if (Float.compare(itemPrefs.itemTextSize, itemTextSize) != 0) {
                return false;
            }
            if (itemPadding != itemPrefs.itemPadding) {
                return false;
            }
            if (Float.compare(itemPrefs.itemShadowRadius, itemShadowRadius) != 0) {
                return false;
            }
            if (itemShadowColor != itemPrefs.itemShadowColor) {
                return false;
            }
            return itemFont == itemPrefs.itemFont;
        }

        @Override
        public int hashCode() {
            int result = (itemTextSize != +0.0f ? Float.floatToIntBits(itemTextSize) : 0);
            result = 31 * result + itemPadding;
            result = 31 * result + (itemShadowRadius != +0.0f ? Float.floatToIntBits(itemShadowRadius) : 0);
            result = 31 * result + itemFont.hashCode();
            result = 31 * result + itemShadowColor;
            return result;
        }

        @Override
        public String toString() {
            return "{" +
                    "itemTextSize=" + itemTextSize +
                    ", itemPadding=" + itemPadding +
                    ", itemShadowRadius=" + itemShadowRadius +
                    ", itemShadowColor=" + String.format("#%08x", itemShadowColor) +
                    ", itemFont=" + itemFont +
                    '}';
        }
    }
}
