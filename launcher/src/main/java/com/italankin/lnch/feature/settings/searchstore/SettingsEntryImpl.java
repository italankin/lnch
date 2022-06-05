package com.italankin.lnch.feature.settings.searchstore;

import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

class SettingsEntryImpl implements SettingsEntry {

    final Key key;
    @StringRes
    final int title;
    @StringRes
    final int summary;
    @StringRes
    final int category;
    final List<SearchTokens> searchTokens;
    final SettingStackBuilder stackBuilder;

    SettingsEntryImpl(
            Key key,
            @StringRes int title,
            @StringRes int summary,
            @StringRes int category,
            List<SearchTokens> searchTokens,
            SettingStackBuilder stackBuilder) {
        this.key = key;
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.searchTokens = searchTokens;
        this.stackBuilder = stackBuilder;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public int title() {
        return title;
    }

    @Override
    public int summary() {
        return summary;
    }

    @Override
    public int category() {
        return category;
    }

    @Override
    public SettingStackBuilder stackBuilder() {
        return stackBuilder;
    }

    static class Builder {
        private final Key key;
        @StringRes
        private int title;
        @StringRes
        private int summary;
        @StringRes
        private int category;
        private final List<SearchTokens> searchTokens = new ArrayList<>(1);
        private SettingStackBuilder stackBuilder;

        Builder(@StringRes int key) {
            this.key = new ResourceKey(key);
        }

        Builder(Preferences.Pref<?> pref) {
            key = new StringKey(pref.key());
        }

        Builder title(@StringRes int title) {
            this.title = title;
            return this;
        }

        Builder summary(@StringRes int summary) {
            this.summary = summary;
            return this;
        }

        Builder category(@StringRes int category) {
            this.category = category;
            return addResourcesSearchTokens(category);
        }

        Builder stackBuilder(SettingStackBuilder stackBuilder) {
            this.stackBuilder = stackBuilder;
            return this;
        }

        Builder addSearchTokens(SearchTokens... tokens) {
            searchTokens.addAll(Arrays.asList(tokens));
            return this;
        }

        Builder addResourcesSearchTokens(@StringRes Integer... resources) {
            return addSearchTokens(SearchTokens.resources(resources));
        }

        Builder addArraysSearchTokens(@ArrayRes Integer... arrays) {
            return addSearchTokens(SearchTokens.arrays(arrays));
        }

        Builder addStringsSearchTokens(String... strings) {
            return addSearchTokens(SearchTokens.strings(strings));
        }

        SettingsEntryImpl build() {
            if (title == 0) {
                throw new IllegalArgumentException(key + " has no title");
            }
            if (category == 0) {
                throw new IllegalArgumentException(key + " has no category");
            }
            if (stackBuilder == null) {
                throw new IllegalArgumentException(key + " has no stackBuilder");
            }
            return new SettingsEntryImpl(key, title, summary, category, searchTokens, stackBuilder);
        }
    }
}
