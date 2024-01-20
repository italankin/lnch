package com.italankin.lnch.model.repository.search;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.EnumSet;
import java.util.List;

public interface SearchDelegate {

    /**
     * @param constraint    search query text
     * @param query         normalized search query text
     * @param searchTargets a set of targets user interested in
     * @return list of found {@link PartialMatch}es
     */
    List<Match> search(CharSequence constraint, String query, EnumSet<Preferences.SearchTarget> searchTargets);
}
