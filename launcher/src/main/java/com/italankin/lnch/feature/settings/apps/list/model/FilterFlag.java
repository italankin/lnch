package com.italankin.lnch.feature.settings.apps.list.model;

import com.italankin.lnch.R;

import androidx.annotation.StringRes;

public enum FilterFlag {
    VISIBLE(R.string.settings_apps_list_filter_visible),
    IGNORED(R.string.settings_apps_list_filter_ignored);

    @StringRes
    public final int title;

    FilterFlag(int title) {
        this.title = title;
    }
}
