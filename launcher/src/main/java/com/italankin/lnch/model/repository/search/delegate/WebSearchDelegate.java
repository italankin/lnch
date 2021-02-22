package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.WebSearchMatch;
import com.italankin.lnch.model.repository.search.web.WebSearchProvider;
import com.italankin.lnch.model.repository.search.web.WebSearchProviderFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class WebSearchDelegate implements SearchDelegate {

    private final Preferences preferences;

    public WebSearchDelegate(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public List<Match> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (searchTargets.contains(Preferences.SearchTarget.WEB)) {
            WebSearchProvider provider = WebSearchProviderFactory.get(preferences);
            WebSearchMatch match = provider.make(query, query);
            if (match != null) {
                return Collections.singletonList(match);
            }
        }
        return Collections.emptyList();
    }
}
