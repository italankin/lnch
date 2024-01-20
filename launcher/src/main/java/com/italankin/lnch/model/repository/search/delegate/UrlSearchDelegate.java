package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.UrlMatch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static android.util.Patterns.WEB_URL;

public class UrlSearchDelegate implements SearchDelegate {
    @Override
    public List<Match> search(CharSequence constraint, String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (searchTargets.contains(Preferences.SearchTarget.URL)) {
            if (WEB_URL.matcher(query).matches() || WEB_URL.matcher("http://" + query).matches()) {
                return Collections.singletonList(new UrlMatch(query));
            }
        }
        return Collections.emptyList();
    }
}
