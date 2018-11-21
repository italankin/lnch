package com.italankin.lnch.feature.settings.apps.model;

import android.support.annotation.StringRes;

import com.italankin.lnch.R;

public enum FilterFlag {
    VISIBLE(R.string.settings_apps_filter_visible),
    HIDDEN(R.string.settings_apps_filter_hidden);

    @StringRes
    public final int title;

    FilterFlag(int title) {
        this.title = title;
    }
}
