package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.editmode.EditModeProperties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EditModeItemPrefs extends ItemPrefsWrapper {

    public static final Set<EditModeState.Property<?>> ITEM_PREFS_PROPERTIES = new HashSet<>(Arrays.asList(
            EditModeProperties.ITEM_TEXT_SIZE,
            EditModeProperties.ITEM_PADDING
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
}
