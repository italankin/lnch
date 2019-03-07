package com.italankin.lnch.feature.settings.apps.list.model;

import com.italankin.lnch.R;

import androidx.annotation.StringRes;

public enum FilterFlag {
    VISIBLE(R.string.settings_apps_list_filter_visible),
    HIDDEN(R.string.settings_apps_list_filter_hidden);

    @StringRes
    public final int title;

    FilterFlag(int title) {
        this.title = title;
    }
}