package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public final class UserPrefs {

    public static final Set<Preferences.Pref<?>> PREFERENCES = new HashSet<>();

    static {
        PREFERENCES.add(Preferences.HOME_LAYOUT);
        PREFERENCES.add(Preferences.HOME_ALIGNMENT);
        PREFERENCES.add(Preferences.WALLPAPER_OVERLAY_COLOR);
        PREFERENCES.add(Preferences.WALLPAPER_OVERLAY_SHOW);
        PREFERENCES.add(Preferences.SHOW_SCROLLBAR);
        PREFERENCES.add(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
        PREFERENCES.add(Preferences.LARGE_SEARCH_BAR);
        PREFERENCES.add(Preferences.ITEM_TEXT_SIZE);
        PREFERENCES.add(Preferences.ITEM_PADDING);
        PREFERENCES.add(Preferences.ITEM_SHADOW_RADIUS);
        PREFERENCES.add(Preferences.ITEM_SHADOW_COLOR);
        PREFERENCES.add(Preferences.ITEM_FONT);
        PREFERENCES.add(Preferences.NOTIFICATION_DOT_COLOR);
    }

    public final Preferences.HomeLayout homeLayout;
    public final Preferences.HomeAlignment homeAlignment;
    @ColorInt
    public final int overlayColor;
    public final boolean showScrollbar;
    public final boolean globalSearch;
    public final boolean largeSearchBar;
    @ColorInt
    public final Integer statusBarColor;
    public final ItemPrefs itemPrefs;

    public UserPrefs(Preferences preferences) {
        homeLayout = preferences.get(Preferences.HOME_LAYOUT);
        homeAlignment = preferences.get(Preferences.HOME_ALIGNMENT);
        overlayColor = preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR);
        showScrollbar = preferences.get(Preferences.SHOW_SCROLLBAR);
        globalSearch = preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
        largeSearchBar = preferences.get(Preferences.LARGE_SEARCH_BAR);
        statusBarColor = preferences.get(Preferences.STATUS_BAR_COLOR);
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
        if (largeSearchBar != userPrefs.largeSearchBar) {
            return false;
        }
        if (homeLayout != userPrefs.homeLayout) {
            return false;
        }
        if (homeAlignment != userPrefs.homeAlignment) {
            return false;
        }
        if (!Objects.equals(statusBarColor, userPrefs.statusBarColor)) {
            return false;
        }
        return itemPrefs.equals(userPrefs.itemPrefs);
    }

    @Override
    public int hashCode() {
        int result = homeLayout.hashCode();
        result = 31 * result + homeAlignment.hashCode();
        result = 31 * result + overlayColor;
        result = 31 * result + (showScrollbar ? 1 : 0);
        result = 31 * result + itemPrefs.hashCode();
        result = 31 * result + (globalSearch ? 1 : 0);
        result = 31 * result + (largeSearchBar ? 1 : 0);
        if (statusBarColor != null) {
            result = 31 * result + statusBarColor.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "homeLayout=" + homeLayout +
                ", homeAlignment=" + homeAlignment +
                ", overlayColor=" + String.format("#%08x", overlayColor) +
                ", showScrollbar=" + showScrollbar +
                ", globalSearch=" + globalSearch +
                ", largeSearchBar=" + largeSearchBar +
                ", statusBarColor=" + statusBarColor +
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
        public final Integer notificationDotColor;

        private ItemPrefs(Preferences preferences) {
            itemTextSize = preferences.get(Preferences.ITEM_TEXT_SIZE);
            itemPadding = preferences.get(Preferences.ITEM_PADDING);
            itemShadowRadius = preferences.get(Preferences.ITEM_SHADOW_RADIUS);
            itemShadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
            itemFont = preferences.get(Preferences.ITEM_FONT);
            notificationDotColor = preferences.get(Preferences.NOTIFICATION_DOT_COLOR);
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
            if (!Objects.equals(notificationDotColor, itemPrefs.notificationDotColor)) {
                return false;
            }
            if (Float.compare(itemPrefs.itemShadowRadius, itemShadowRadius) != 0) {
                return false;
            }
            if (!Objects.equals(itemShadowColor, itemPrefs.itemShadowColor)) {
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
            if (notificationDotColor != null) {
                result = 31 * result + notificationDotColor;
            }
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
                    ", itemShadowColor=" +
                    (itemShadowColor != null ? String.format("#%08x", itemShadowColor) : "default") +
                    ", itemFont=" + itemFont +
                    ", notificationDotColor=" + notificationDotColor +
                    '}';
        }
    }
}
