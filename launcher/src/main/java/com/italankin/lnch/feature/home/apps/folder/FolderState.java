package com.italankin.lnch.feature.home.apps.folder;

import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import java.util.List;

class FolderState {
    final FolderDescriptorUi folder;
    final List<DescriptorUi> items;
    final UserPrefs userPrefs;
    final boolean animated;

    FolderState(FolderDescriptorUi folder, List<DescriptorUi> items, UserPrefs userPrefs, boolean animated) {
        this.folder = folder;
        this.items = items;
        this.userPrefs = userPrefs;
        this.animated = animated;
    }
}
