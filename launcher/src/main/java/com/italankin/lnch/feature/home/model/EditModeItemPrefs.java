package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.editmode.EditModeProperties;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EditModeItemPrefs extends ItemPrefsWrapper {

    public static final Set<EditModeState.Property<?>> ITEM_PREFS_PROPERTIES = new HashSet<>(Arrays.asList(
            EditModeProperties.ITEM_TEXT_SIZE,
            EditModeProperties.ITEM_PADDING,
            EditModeProperties.ITEM_WIDTH,
            EditModeProperties.HOME_ALIGNMENT
    ));

    private final EditModeState editModeState;

    public EditModeItemPrefs(EditModeState editModeState, UserPrefs.ItemPrefs itemPrefs) {
        super(itemPrefs);
        this.editModeState = editModeState;
    }

    @Override
    public float itemTextSize() {
        if (!editModeState.isActive()) {
            return super.itemTextSize();
        }
        Float itemTextSize = editModeState.getProperty(EditModeProperties.ITEM_TEXT_SIZE);
        return itemTextSize != null ? itemTextSize : super.itemTextSize();
    }

    @Override
    public int itemPadding() {
        if (!editModeState.isActive()) {
            return super.itemPadding();
        }
        Integer itemPadding = editModeState.getProperty(EditModeProperties.ITEM_PADDING);
        return itemPadding != null ? itemPadding : super.itemPadding();
    }
    @Override
    public Preferences.ItemWidth itemWidth() {
        if (!editModeState.isActive()) {
            return super.itemWidth();
        }
        Preferences.ItemWidth width = editModeState.getProperty(EditModeProperties.ITEM_WIDTH);
        return width != null ? width : super.itemWidth();
    }

    @Override
    public Preferences.HomeAlignment homeAlignment() {
        if (!editModeState.isActive()) {
            return super.homeAlignment();
        }
        Preferences.HomeAlignment alignment = editModeState.getProperty(EditModeProperties.HOME_ALIGNMENT);
        return alignment != null ? alignment : super.homeAlignment();
    }
}
