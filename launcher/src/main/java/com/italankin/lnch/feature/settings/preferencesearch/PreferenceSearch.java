package com.italankin.lnch.feature.settings.preferencesearch;

import androidx.annotation.StringRes;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;

class PreferenceSearch implements PreferenceSearchItem {

    final SettingsEntry.Key key;
    @StringRes
    final int title;
    @StringRes
    final int summary;

    PreferenceSearch(SettingsEntry entry) {
        key = entry.key();
        title = entry.title();
        summary = entry.summary();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
