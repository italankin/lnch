package com.italankin.lnch.model.repository.search.web;

import android.net.Uri;

import com.italankin.lnch.model.repository.search.match.WebSearchMatch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class StaticProvider implements WebSearchProvider {
    private final String prefix;

    StaticProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public WebSearchMatch make(String label, String query) {
        return new WebSearchMatch(label, Uri.parse(prefix + sanitizeQuery(query)));
    }

    private static String sanitizeQuery(String query) {
        try {
            return URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return query;
        }
    }
}
