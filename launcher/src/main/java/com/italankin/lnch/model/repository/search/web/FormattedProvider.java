package com.italankin.lnch.model.repository.search.web;

import android.net.Uri;

import com.italankin.lnch.model.repository.search.match.WebSearchMatch;

import java.util.Locale;

import androidx.annotation.Nullable;

class FormattedProvider implements WebSearchProvider {
    private final String format;

    FormattedProvider(String format) {
        this.format = format;
    }

    @Nullable
    @Override
    public WebSearchMatch make(String label, String query) {
        String uri = format(query);
        if (uri == null) {
            return null;
        }
        return new WebSearchMatch(label, Uri.parse(uri));
    }

    private String format(String query) {
        try {
            return String.format(Locale.ROOT, format, Uri.encode(query));
        } catch (Exception e) {
            return null;
        }
    }
}
