package com.italankin.lnch.feature.home.repository.editmode;

import com.italankin.lnch.feature.home.repository.EditModeState;

public final class EditModeProperties {

    public static final EditModeState.Property<Integer> WALLPAPER_DIM = new WallpaperDimProperty();

    private EditModeProperties() {
    }
}
