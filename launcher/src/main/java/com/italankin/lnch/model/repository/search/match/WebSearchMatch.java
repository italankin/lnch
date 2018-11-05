package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import com.italankin.lnch.R;

public class WebSearchMatch extends PartialMatch {

    public WebSearchMatch(String label, Uri uri) {
        super(Type.OTHER);
        this.label = label.trim();
        color = Color.WHITE;
        iconRes = R.drawable.ic_search;
        intent = new Intent(Intent.ACTION_VIEW, uri);
    }
}
