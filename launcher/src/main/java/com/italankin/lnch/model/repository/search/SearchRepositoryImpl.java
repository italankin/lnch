package com.italankin.lnch.model.repository.search;

import android.content.Context;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.search.match.UrlMatch;
import com.italankin.lnch.model.repository.search.web.WebSearchProvider;
import com.italankin.lnch.model.repository.search.web.WebSearchProviderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static android.util.Patterns.WEB_URL;
import static com.italankin.lnch.model.repository.prefs.Preferences.SearchTarget;

public class SearchRepositoryImpl implements SearchRepository {

    private static final int MAX_RESULTS = 4;

    private final Context context;
    private final List<SearchDelegate> delegates;
    private final Preferences preferences;

    public SearchRepositoryImpl(Context context, List<SearchDelegate> delegates, Preferences preferences) {
        this.context = context;
        this.preferences = preferences;
        this.delegates = delegates;
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

        EnumSet<SearchTarget> searchTargets = preferences.searchTargets();
        List<PartialMatch> matches = new ArrayList<>(8);
        for (SearchDelegate delegate : delegates) {
            List<PartialMatch> list = delegate.search(query, searchTargets);
            matches.addAll(list);
        }

        if (matches.size() > 1) {
            Collections.sort(matches);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }

        if (searchTargets.contains(SearchTarget.WEB)) {
            WebSearchProvider webSearchProvider = WebSearchProviderFactory.get(preferences.searchEngine());
            matches.add(webSearchProvider.make(constraint.toString(), query));
        }

        if (searchTargets.contains(SearchTarget.URL)) {
            if (WEB_URL.matcher(query).matches() || WEB_URL.matcher("http://" + query).matches()) {
                matches.add(new UrlMatch(context, query));
            }
        }

        return matches;
    }
}

