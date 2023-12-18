package com.italankin.lnch.feature.home.model;

import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class ItemPrefsWrapper implements UserPrefs.ItemPrefs {
    private final UserPrefs.ItemPrefs itemPrefs;

    public ItemPrefsWrapper(UserPrefs.ItemPrefs itemPrefs) {
        this.itemPrefs = itemPrefs;
    }

    @Override
    public float itemTextSize() {
        return itemPrefs.itemTextSize();
    }

    @Override
    public int itemPadding() {
        return itemPrefs.itemPadding();
    }

    @Override
    public float itemShadowRadius() {
        return itemPrefs.itemShadowRadius();
    }

    @Nullable
    @Override
    public Integer itemShadowColor() {
        return itemPrefs.itemShadowColor();
    }

    @NonNull
    @Override
    public Typeface typeface() {
        return itemPrefs.typeface();
    }

    @Nullable
    @Override
    public Integer notificationDotColor() {
        return itemPrefs.notificationDotColor();
    }

    @Override
    public Preferences.NotificationDotSize notificationDotSize() {
        return itemPrefs.notificationDotSize();
    }

    @Override
    public Preferences.ItemWidth itemWidth() {
        return itemPrefs.itemWidth();
    }

    @Override
    public Preferences.HomeAlignment homeAlignment() {
        return itemPrefs.homeAlignment();
    }
}
