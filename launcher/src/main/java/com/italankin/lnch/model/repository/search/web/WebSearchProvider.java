package com.italankin.lnch.model.repository.search.web;

import com.italankin.lnch.model.repository.search.match.WebSearchMatch;

import androidx.annotation.Nullable;

public interface WebSearchProvider {

    @Nullable
    WebSearchMatch make(String label, String query);
}
