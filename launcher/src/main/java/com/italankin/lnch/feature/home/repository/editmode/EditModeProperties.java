package com.italankin.lnch.feature.home.repository.editmode;

import com.italankin.lnch.feature.home.repository.EditModeState.Property;
import com.italankin.lnch.model.repository.prefs.Preferences;

public final class EditModeProperties {

    public static final Property<Integer> WALLPAPER_DIM = new PreferenceProperty<>(Preferences.WALLPAPER_DIM_COLOR);
    public static final Property<Float> ITEM_TEXT_SIZE = new PreferenceProperty<>(Preferences.ITEM_TEXT_SIZE);
    public static final Property<Integer> ITEM_PADDING = new PreferenceProperty<>(Preferences.ITEM_PADDING);

    public static final Property<Preferences.ItemWidth> ITEM_WIDTH = new PreferenceProperty<>(Preferences.ITEM_WIDTH);
    public static final Property<Preferences.HomeAlignment> HOME_ALIGNMENT = new PreferenceProperty<>(Preferences.HOME_ALIGNMENT);

    private EditModeProperties() {
    }
}
