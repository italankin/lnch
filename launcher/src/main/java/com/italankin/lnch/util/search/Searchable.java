package com.italankin.lnch.util.search;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * An item on which search can be performed
 */
public interface Searchable {

    /**
     * Create search tokens from string array.
     *
     * @param tokens strings to search in, might contain {@code null}s
     * @return {@link Set} suitable for {@link #getSearchTokens()}
     */
    static Set<String> createTokens(String... tokens) {
        Set<String> set = new HashSet<>(tokens.length);
        for (String token : tokens) {
            if (token != null) {
                set.add(token);
            }
        }
        return set;
    }

    /**
     * @return whether case-sensitive search should be performed
     */
    default boolean isSearchCaseSensitive() {
        return false;
    }

    /**
     * @return {@link Set} of tokens to perform search on
     */
    Set<String> getSearchTokens();

    default boolean matches(String term) {
        return match(term) != null;
    }

    @Nullable
    default Match match(String term) {
        if (term == null || term.isEmpty()) {
            return null;
        }
        return SearchUtils.match(this, term);
    }

    enum Match {
        EXACT,
        START,
        WORD,
        SUBSTRING
    }
}
