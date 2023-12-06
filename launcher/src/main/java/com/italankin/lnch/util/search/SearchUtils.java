package com.italankin.lnch.util.search;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Set;

public final class SearchUtils {

    public static boolean contains(String what, String substring) {
        return contains(what, substring, false);
    }

    public static boolean contains(String what, String substring, boolean caseSensitive) {
        if (what == null || substring == null) {
            return false;
        }
        if (caseSensitive) {
            return what.contains(substring);
        } else {
            return what.toLowerCase(Locale.getDefault()).contains(substring);
        }
    }

    public static boolean containsWord(String what, String word, boolean caseSensitive) {
        if (what == null || word == null) {
            return false;
        }
        String[] words;
        if (caseSensitive) {
            words = what.split("\\s+");
        } else {
            words = what.toLowerCase(Locale.getDefault()).split("\\s+");
        }
        for (String w : words) {
            if (w.startsWith(word)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(String what, String prefix, boolean caseSensitive) {
        if (what == null || prefix == null) {
            return false;
        }
        if (caseSensitive) {
            return what.startsWith(prefix);
        } else {
            return what.toLowerCase(Locale.getDefault()).startsWith(prefix);
        }
    }

    @Nullable
    public static Searchable.Match match(Searchable searchable, String term) {
        if (term.isEmpty()) {
            return null;
        }
        boolean caseSensitive = searchable.isSearchCaseSensitive();
        Set<String> searchTokens = searchable.getSearchTokens();
        if (caseSensitive && searchTokens.contains(term)) {
            return Searchable.Match.EXACT;
        }
        for (String token : searchTokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (token.length() == term.length() && token.equalsIgnoreCase(term)) {
                return Searchable.Match.EXACT;
            }
            if (startsWith(token, term, caseSensitive)) {
                return Searchable.Match.START;
            }
        }
        for (String token : searchTokens) {
            if (containsWord(token, term, caseSensitive)) {
                return Searchable.Match.WORD;
            }
        }
        for (String token : searchTokens) {
            if (contains(token, term, caseSensitive)) {
                return Searchable.Match.SUBSTRING;
            }
        }
        return null;
    }

    private SearchUtils() {
        // no instance
    }
}
