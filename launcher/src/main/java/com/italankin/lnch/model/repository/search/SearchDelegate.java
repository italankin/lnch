package com.italankin.lnch.model.repository.search;

import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.EnumSet;
import java.util.List;

public interface SearchDelegate {

    List<PartialMatch> search(String query, EnumSet<Preferences.SearchTarget> searchTargets);

}
