package com.italankin.lnch.feature.settings.preferencesearch;

import com.italankin.lnch.feature.settings.searchstore.SettingStackBuilder;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;

import androidx.annotation.StringRes;

class PreferenceSearch implements PreferenceSearchItem {

    final SettingsEntry.Key key;
    @StringRes
    final int title;
    @StringRes
    final int summary;

    final SettingStackBuilder stackBuilder;

    PreferenceSearch(SettingsEntry entry) {
        key = entry.key();
        title = entry.title();
        summary = entry.summary();
        stackBuilder = entry.stackBuilder();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
