package com.italankin.lnch.feature.settings.searchstore;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.search.Searchable;
import timber.log.Timber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.italankin.lnch.util.search.SearchUtils.match;

public class SettingsStore {

    private final Context context;

    private final Map<SettingsEntryImpl, List<SearchToken>> entriesCache = new ConcurrentHashMap<>(Preferences.ALL.size());

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
        Set<Map.Entry<SettingsEntryImpl, List<SearchToken>>> entries = entriesCache.entrySet();
        for (Map.Entry<SettingsEntryImpl, List<SearchToken>> mapEntry : entries) {
            SettingsEntryImpl entry = mapEntry.getKey();
            for (SearchToken searchToken : mapEntry.getValue()) {
                Searchable.Match match = match(searchToken, query);
                if (match != null) {
                    matches.add(new EntryMatch(entry, searchToken.field, EntryMatch.Rank.fromSearchable(match)));
                }
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

    private void populateCache() {
        if (!entriesCache.isEmpty()) {
            return;
        }
        long start = System.nanoTime();
        for (SettingsEntryImpl entry : SettingsEntries.entries()) {
            if (!entry.isAvailable) {
                continue;
            }
            List<SearchToken> settingsTokens = new ArrayList<>(1);
            settingsTokens.add(new SearchToken(
                    EntryMatch.Field.TITLE,
                    Collections.singleton(context.getString(entry.title))));
            if (entry.summary != 0) {
                settingsTokens.add(new SearchToken(
                        EntryMatch.Field.SUMMARY,
                        Collections.singleton(context.getString(entry.summary))
                ));
            }
            if (!entry.searchTokens.isEmpty()) {
                Set<String> tokens = new HashSet<>(4);
                for (SearchTokens searchTokens : entry.searchTokens) {
                    tokens.addAll(searchTokens.get(context));
                }
                settingsTokens.add(new SearchToken(EntryMatch.Field.OTHER, tokens));
            }

            entriesCache.put(entry, settingsTokens);
        }
        Timber.d("populateCache: entries=%d, done in %.3fms",
                entriesCache.size(), (System.nanoTime() - start) / 1_000_000f);
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

    private static class SearchToken implements Searchable {
        final EntryMatch.Field field;
        private final Set<String> tokens;

        SearchToken(EntryMatch.Field field, Set<String> tokens) {
            this.field = field;
            this.tokens = tokens;
        }

        @Override
        public Set<String> getSearchTokens() {
            return tokens;
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
            EXACT, STARTS_WITH, CONTAINS_WORD, CONTAINS;

            @Nullable
            static Rank fromSearchable(@Nullable Searchable.Match match) {
                if (match == null) {
                    return null;
                }
                switch (match) {
                    case EXACT:
                        return Rank.EXACT;
                    case START:
                        return Rank.STARTS_WITH;
                    case WORD:
                        return Rank.CONTAINS_WORD;
                    case SUBSTRING:
                        return Rank.CONTAINS;
                }
                return null;
            }
        }
    }
}
