package com.italankin.lnch.model.repository.search.web;

import com.italankin.lnch.model.repository.search.match.WebSearchMatch;

public interface WebSearchProvider {

    WebSearchMatch make(String label, String query);
}
