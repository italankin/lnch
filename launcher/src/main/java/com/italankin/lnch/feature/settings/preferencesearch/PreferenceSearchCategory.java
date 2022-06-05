package com.italankin.lnch.feature.settings.preferencesearch;

import androidx.annotation.StringRes;

class PreferenceSearchCategory implements PreferenceSearchItem {

    @StringRes
    public final int category;

    PreferenceSearchCategory(@StringRes int category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        return category;
    }
}
