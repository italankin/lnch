package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import com.italankin.lnch.R;

public class UrlMatch extends PartialMatch {
    public UrlMatch(String url) {
        super(Type.OTHER);
        color = Color.WHITE;
        label = buildLabel(url);
        Uri uri;
        if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
            uri = Uri.parse(url);
        } else {
            uri = Uri.parse("http://" + url);
        }
        intent = new Intent(Intent.ACTION_VIEW, uri);
        iconRes = R.drawable.ic_open_url;
    }

    private CharSequence buildLabel(String url) {
        return new SpannableStringBuilder("Go to ")
                .append(url, new UnderlineSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
