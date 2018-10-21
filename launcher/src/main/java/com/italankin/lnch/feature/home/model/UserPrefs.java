package com.italankin.lnch.feature.home.model;

import android.graphics.Typeface;

import com.italankin.lnch.model.repository.prefs.Preferences;

public final class UserPrefs {
    public Preferences.HomeLayout homeLayout;
    public int overlayColor;
    public boolean showScrollbar;
    public ItemPrefs itemPrefs;

    public static final class ItemPrefs {
        public float itemTextSize;
        public int itemPadding;
        public float itemShadowRadius;
        public Typeface itemFont;

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
            return itemFont.equals(itemPrefs.itemFont);
        }

        @Override
        public int hashCode() {
            int result = (itemTextSize != +0.0f ? Float.floatToIntBits(itemTextSize) : 0);
            result = 31 * result + itemPadding;
            result = 31 * result + (itemShadowRadius != +0.0f ? Float.floatToIntBits(itemShadowRadius) : 0);
            result = 31 * result + itemFont.hashCode();
            return result;
        }
    }
}
