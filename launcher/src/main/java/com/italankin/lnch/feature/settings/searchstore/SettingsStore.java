package com.italankin.lnch.feature.settings.searchstore;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.repository.prefs.Preferences;
import timber.log.Timber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.italankin.lnch.util.SearchUtils.*;

public class SettingsStore {

    public static final char SEARCH_TOKEN_DELIMITER = '\n';

    private final Context context;

    private final Map<SettingsEntryImpl, SearchToken> entriesCache = new ConcurrentHashMap<>(Preferences.ALL.size());

    public SettingsStore(Context context) {
        this.context = context;
    }

    public List<SettingsEntry> search(String query) {
        return search(query, -1);
    }

    public List<SettingsEntry> search(String query, int maxResults) {
        populateCache();
        long start = System.nanoTime();
        List<EntryMatch> matches = new ArrayList<>(10);
        Set<Map.Entry<SettingsEntryImpl, SearchToken>> entries = entriesCache.entrySet();
        for (Map.Entry<SettingsEntryImpl, SearchToken> mapEntry : entries) {
            SettingsEntryImpl entry = mapEntry.getKey();
            SearchToken searchToken = mapEntry.getValue();
            EntryMatch entryMatch = testEntry(entry, searchToken, query);
            if (entryMatch != null) {
                matches.add(entryMatch);
            }
        }
        Collections.sort(matches);
        List<SettingsEntry> results = new ArrayList<>(matches.size());
        for (EntryMatch match : matches) {
            results.add(match.entry);
            if (results.size() == maxResults) {
                break;
            }
        }
        Timber.d("search: query='%s', results=%d, done in %.3fms",
                query, results.size(), (System.nanoTime() - start) / 1_000_000f);
        return results;
    }

    @Nullable
    public SettingsEntry find(@NonNull Preferences.Pref<?> pref) {
        for (SettingsEntryImpl settingsEntry : entriesCache.keySet()) {
            if (pref.equals(settingsEntry.pref)) {
                return settingsEntry;
            }
        }
        return null;
    }

    @Nullable
    private EntryMatch testEntry(SettingsEntryImpl entry, SearchToken searchToken, String query) {
        if (startsWith(searchToken.title, query)) {
            return new EntryMatch(entry, EntryMatch.Field.TITLE, EntryMatch.Rank.STARTS_WITH);
        } else if (containsWord(searchToken.title, query)) {
            return new EntryMatch(entry, EntryMatch.Field.TITLE, EntryMatch.Rank.CONTAINS_WORD);
        } else if (contains(searchToken.title, query)) {
            return new EntryMatch(entry, EntryMatch.Field.TITLE, EntryMatch.Rank.CONTAINS);
        }
        if (searchToken.summary != null) {
            if (startsWith(searchToken.summary, query)) {
                return new EntryMatch(entry, EntryMatch.Field.SUMMARY, EntryMatch.Rank.STARTS_WITH);
            } else if (containsWord(searchToken.summary, query)) {
                return new EntryMatch(entry, EntryMatch.Field.SUMMARY, EntryMatch.Rank.CONTAINS_WORD);
            } else if (contains(searchToken.summary, query)) {
                return new EntryMatch(entry, EntryMatch.Field.SUMMARY, EntryMatch.Rank.CONTAINS);
            }
        }
        if (!searchToken.otherTokens.isEmpty()) {
            if (startsWith(searchToken.otherTokens, query)) {
                return new EntryMatch(entry, EntryMatch.Field.OTHER, EntryMatch.Rank.STARTS_WITH);
            } else if (containsWord(searchToken.otherTokens, query)) {
                return new EntryMatch(entry, EntryMatch.Field.OTHER, EntryMatch.Rank.CONTAINS_WORD);
            } else if (contains(searchToken.otherTokens, query)) {
                return new EntryMatch(entry, EntryMatch.Field.OTHER, EntryMatch.Rank.CONTAINS);
            }
        }

        return null;
    }

    private void populateCache() {
        if (entriesCache.isEmpty()) {
            long start = System.nanoTime();
            for (SettingsEntryImpl entry : SettingsEntries.entries()) {
                StringBuilder otherTokens = new StringBuilder(entry.searchTokens.size() * 10);
                for (SearchTokens searchToken : entry.searchTokens) {
                    List<String> tokens = searchToken.get(context);
                    for (String token : tokens) {
                        otherTokens.append(SEARCH_TOKEN_DELIMITER);
                        otherTokens.append(token);
                    }
                }
                entriesCache.put(entry, new SearchToken(
                        context.getString(entry.title).toLowerCase(Locale.getDefault()),
                        entry.summary != 0
                                ? context.getString(entry.summary).toLowerCase(Locale.getDefault())
                                : null,
                        otherTokens.toString().toLowerCase(Locale.getDefault())
                ));
            }
            Timber.d("populateCache: entries=%d, done in %.3fms",
                    entriesCache.size(), (System.nanoTime() - start) / 1_000_000f);
        }
    }

    @Nullable
    public SettingsEntry getByKey(SettingsEntry.Key key) {
        populateCache();
        for (SettingsEntryImpl entry : entriesCache.keySet()) {
            if (entry.key.equals(key)) {
                return entry;
            }
        }
        return null;
    }

    private static class SearchToken {
        final String title;
        final String summary;
        final String otherTokens;

        SearchToken(String title, String summary, String otherTokens) {
            this.title = title;
            this.summary = summary;
            this.otherTokens = otherTokens;
        }
    }

    private static class EntryMatch implements Comparable<EntryMatch> {
        final SettingsEntry entry;
        final Field field;
        final Rank rank;

        EntryMatch(SettingsEntry entry, Field field, Rank rank) {
            this.entry = entry;
            this.field = field;
            this.rank = rank;
        }

        @Override
        public int compareTo(EntryMatch o) {
            int c = field.compareTo(o.field);
            if (c == 0) {
                return rank.compareTo(o.rank);
            } else {
                return c;
            }
        }

        enum Field {
            TITLE, SUMMARY, OTHER
        }

        enum Rank {
            STARTS_WITH, CONTAINS_WORD, CONTAINS
        }
    }
}
