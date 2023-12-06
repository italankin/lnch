package com.italankin.lnch.feature.settings.searchstore;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    final boolean isAvailable;
    @Nullable
    final Preferences.Pref<?> pref;

    SettingsEntryImpl(
            Key key,
            @Nullable Preferences.Pref<?> pref,
            @StringRes int title,
            @StringRes int summary,
            @StringRes int category,
            List<SearchTokens> searchTokens,
            SettingStackBuilder stackBuilder,
            boolean isAvailable) {
        this.key = key;
        this.pref = pref;
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.searchTokens = searchTokens;
        this.stackBuilder = stackBuilder;
        this.isAvailable = isAvailable;
    }

    @Override
    public Key key() {
        return key;
    }

    @Nullable
    @Override
    public Preferences.Pref<?> preference() {
        return pref;
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
        @Nullable
        private final Preferences.Pref<?> pref;
        @StringRes
        private int title;
        @StringRes
        private int summary;
        @StringRes
        private int category;
        private boolean available = true;
        private final List<SearchTokens> searchTokens = new ArrayList<>(1);
        private SettingStackBuilder stackBuilder;

        Builder(@StringRes int key) {
            this.key = new ResourceKey(key);
            this.pref = null;
        }

        Builder(Preferences.Pref<?> pref) {
            key = new StringKey(pref.key());
            this.pref = pref;
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

        Builder setAvailable(boolean available) {
            this.available = available;
            return this;
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
            return new SettingsEntryImpl(key, pref, title, summary, category, searchTokens, stackBuilder, available);
        }
    }
}
