package com.italankin.lnch.model.repository.search;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.italankin.lnch.model.repository.prefs.Preferences.SearchTarget;

public class SearchRepositoryImpl implements SearchRepository {

    private static final int MAX_RESULTS = 4;
    private static final Comparator<Match> MATCH_COMPARATOR = new MatchComparator();

    private final List<SearchDelegate> delegates;
    private final List<SearchDelegate> additionalDelegates;
    private final Preferences preferences;

    /**
     * @param delegates           search delegates
     * @param additionalDelegates additional search delegates, which can add their results above {@link #MAX_RESULTS} limit
     */
    public SearchRepositoryImpl(
            List<SearchDelegate> delegates,
            List<SearchDelegate> additionalDelegates,
            Preferences preferences
    ) {
        this.delegates = delegates;
        this.additionalDelegates = additionalDelegates;
        this.preferences = preferences;
    }

    @Override
    public List<? extends Match> search(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return Collections.emptyList();
        }
        String query = constraint.toString().trim().toLowerCase(Locale.getDefault());
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        EnumSet<SearchTarget> searchTargets = preferences.get(Preferences.SEARCH_TARGETS);
        List<Match> matches = new ArrayList<>(8);
        for (SearchDelegate delegate : delegates) {
            List<Match> list = delegate.search(query, searchTargets);
            matches.addAll(list);
        }
        if (matches.size() > 1) {
            Collections.sort(matches, MATCH_COMPARATOR);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }
        for (SearchDelegate delegate : additionalDelegates) {
            List<Match> list = delegate.search(query, searchTargets);
            matches.addAll(list);
        }
        return matches;
    }

    @Override
    public List<? extends Match> recent() {
        return Collections.emptyList(); // TODO
    }

    private static final class MatchComparator implements Comparator<Match> {
        @Override
        public int compare(Match lhs, Match rhs) {
            int compareKind = lhs.getKind().compareTo(rhs.getKind());
            if (compareKind != 0) {
                return compareKind;
            }
            if (lhs instanceof PartialMatch && rhs instanceof PartialMatch) {
                return ((PartialMatch) lhs).compareTo(((PartialMatch) rhs));
            }
            return 0;
        }
    }
}

