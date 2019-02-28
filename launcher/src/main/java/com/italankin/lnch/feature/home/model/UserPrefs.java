package com.italankin.lnch.feature.home.model;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.italankin.lnch.model.repository.prefs.Preferences;

public final class UserPrefs {
    public final Preferences.HomeLayout homeLayout;
    @ColorInt
    public final int overlayColor;
    public final boolean showScrollbar;
    public final boolean globalSearch;
    public final ItemPrefs itemPrefs;

    public UserPrefs(Preferences preferences) {
        homeLayout = preferences.get(Preferences.HOME_LAYOUT);
        overlayColor = preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR);
        showScrollbar = preferences.get(Preferences.SHOW_SCROLLBAR);
        globalSearch = preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
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
        public final float itemTextSize;
        public final int itemPadding;
        public final float itemShadowRadius;
        @ColorInt
        @Nullable
        public final Integer itemShadowColor;
        public final Preferences.Font itemFont;

        private ItemPrefs(Preferences preferences) {
            itemTextSize = preferences.get(Preferences.ITEM_TEXT_SIZE);
            itemPadding = preferences.get(Preferences.ITEM_PADDING);
            itemShadowRadius = preferences.get(Preferences.ITEM_SHADOW_RADIUS);
            itemShadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
            itemFont = preferences.get(Preferences.ITEM_FONT);
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
            if (itemShadowColor != null
                    ? itemShadowColor.equals(itemPrefs.itemShadowColor)
                    : itemPrefs.itemShadowColor != null) {
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
            if (itemShadowColor != null) {
                result = 31 * result + itemShadowColor;
            }
            return result;
        }

        @Override
        public String toString() {
            return "{" +
                    "itemTextSize=" + itemTextSize +
                    ", itemPadding=" + itemPadding +
                    ", itemShadowRadius=" + itemShadowRadius +
                    ", itemShadowColor=" + (itemShadowColor != null ? String.format("#%08x", itemShadowColor) : "default") +
                    ", itemFont=" + itemFont +
                    '}';
        }
    }
}
