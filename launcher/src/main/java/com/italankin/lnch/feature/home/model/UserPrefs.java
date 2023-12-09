package com.italankin.lnch.feature.home.model;

import android.graphics.Typeface;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class UserPrefs {

    public static final Set<Preferences.Pref<?>> PREFERENCES = new HashSet<>();

    static {
        PREFERENCES.add(Preferences.HOME_LAYOUT);
        PREFERENCES.add(Preferences.HOME_ALIGNMENT);
        PREFERENCES.add(Preferences.WALLPAPER_OVERLAY_COLOR);
        PREFERENCES.add(Preferences.WALLPAPER_OVERLAY_SHOW);
        PREFERENCES.add(Preferences.SHOW_SCROLLBAR);
        PREFERENCES.add(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
        PREFERENCES.add(Preferences.SEARCH_SHOW_CUSTOMIZE);
        PREFERENCES.add(Preferences.LARGE_SEARCH_BAR);
        PREFERENCES.add(Preferences.STATUS_BAR_COLOR);
        PREFERENCES.add(Preferences.ITEM_TEXT_SIZE);
        PREFERENCES.add(Preferences.ITEM_PADDING);
        PREFERENCES.add(Preferences.ITEM_SHADOW_RADIUS);
        PREFERENCES.add(Preferences.ITEM_SHADOW_COLOR);
        PREFERENCES.add(Preferences.ITEM_FONT);
        PREFERENCES.add(Preferences.ITEM_WIDTH);
        PREFERENCES.add(Preferences.NOTIFICATION_DOT_COLOR);
        PREFERENCES.add(Preferences.NOTIFICATION_DOT_SIZE);
    }

    public final Preferences.HomeLayout homeLayout;
    public final Preferences.HomeAlignment homeAlignment;
    @ColorInt
    public final int overlayColor;
    public final boolean showScrollbar;
    @ColorInt
    public final Integer statusBarColor;
    public final ItemPrefs itemPrefs;
    public final boolean globalSearch;
    public final boolean largeSearchBar;
    public final boolean searchBarShowCustomize;

    public UserPrefs(Preferences preferences, FontManager fontManager) {
        homeLayout = preferences.get(Preferences.HOME_LAYOUT);
        homeAlignment = preferences.get(Preferences.HOME_ALIGNMENT);
        overlayColor = preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR);
        showScrollbar = preferences.get(Preferences.SHOW_SCROLLBAR);
        globalSearch = preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
        largeSearchBar = preferences.get(Preferences.LARGE_SEARCH_BAR);
        statusBarColor = preferences.get(Preferences.STATUS_BAR_COLOR);
        searchBarShowCustomize = preferences.get(Preferences.SEARCH_SHOW_CUSTOMIZE);
        itemPrefs = new UserPrefs.ItemPrefs(preferences, fontManager);
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
        if (searchBarShowCustomize != userPrefs.searchBarShowCustomize) {
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
        result = 31 * result + (searchBarShowCustomize ? 1 : 0);
        result = 31 * result + itemPrefs.hashCode();
        result = 31 * result + (globalSearch ? 1 : 0);
        result = 31 * result + (largeSearchBar ? 1 : 0);
        if (statusBarColor != null) {
            result = 31 * result + statusBarColor.hashCode();
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "homeLayout=" + homeLayout +
                ", homeAlignment=" + homeAlignment +
                ", overlayColor=" + String.format("#%08x", overlayColor) +
                ", showScrollbar=" + showScrollbar +
                ", globalSearch=" + globalSearch +
                ", globalSearch=" + searchBarShowCustomize +
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
        public final String itemFont;
        public final Typeface typeface;
        public final Integer notificationDotColor;
        public final Preferences.NotificationDotSize notificationDotSize;
        public final Preferences.ItemWidth itemWidth;
        public final Preferences.HomeAlignment alignment;

        private ItemPrefs(Preferences preferences, FontManager fontManager) {
            itemTextSize = preferences.get(Preferences.ITEM_TEXT_SIZE);
            itemPadding = preferences.get(Preferences.ITEM_PADDING);
            itemShadowRadius = preferences.get(Preferences.ITEM_SHADOW_RADIUS);
            itemShadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
            itemFont = preferences.get(Preferences.ITEM_FONT);
            typeface = fontManager.getTypeface(itemFont);
            notificationDotColor = preferences.get(Preferences.NOTIFICATION_DOT_COLOR);
            notificationDotSize = preferences.get(Preferences.NOTIFICATION_DOT_SIZE);
            itemWidth = preferences.get(Preferences.ITEM_WIDTH);
            alignment = preferences.get(Preferences.HOME_ALIGNMENT);
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
            if (itemWidth != itemPrefs.itemWidth) {
                return false;
            }
            if (alignment != itemPrefs.alignment) {
                return false;
            }
            if (!Objects.equals(notificationDotColor, itemPrefs.notificationDotColor)) {
                return false;
            }
            if (notificationDotSize != itemPrefs.notificationDotSize) {
                return false;
            }
            if (Float.compare(itemPrefs.itemShadowRadius, itemShadowRadius) != 0) {
                return false;
            }
            if (!Objects.equals(itemShadowColor, itemPrefs.itemShadowColor)) {
                return false;
            }
            return itemFont.equals(itemPrefs.itemFont);
        }

        @Override
        public int hashCode() {
            int result = (itemTextSize != 0f ? Float.floatToIntBits(itemTextSize) : 0);
            result = 31 * result + itemPadding;
            result = 31 * result + itemWidth.hashCode();
            result = 31 * result + alignment.hashCode();
            result = 31 * result + (itemShadowRadius != 0f ? Float.floatToIntBits(itemShadowRadius) : 0);
            result = 31 * result + itemFont.hashCode();
            if (notificationDotColor != null) {
                result = 31 * result + notificationDotColor;
            }
            result = 31 * result + notificationDotSize.hashCode();
            if (itemShadowColor != null) {
                result = 31 * result + itemShadowColor;
            }
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "{" +
                    "itemTextSize=" + itemTextSize +
                    ", itemPadding=" + itemPadding +
                    ", itemWidth=" + itemWidth +
                    ", alignment=" + alignment +
                    ", itemShadowRadius=" + itemShadowRadius +
                    ", itemShadowColor=" +
                    (itemShadowColor != null ? String.format("#%08x", itemShadowColor) : "default") +
                    ", itemFont=" + itemFont +
                    ", notificationDotColor=" + notificationDotColor +
                    ", notificationDotSize=" + notificationDotSize +
                    '}';
        }
    }
}
