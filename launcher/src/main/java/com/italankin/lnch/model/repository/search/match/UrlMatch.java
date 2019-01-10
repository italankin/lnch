package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import com.italankin.lnch.R;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class UrlMatch extends PartialMatch {

    private static final Set<Action> ACTIONS = Collections.singleton(Action.PIN);

    public UrlMatch(Context context, String url) {
        super(Type.OTHER);
        color = Color.WHITE;
        label = buildLabel(context, url);
        actions = ACTIONS;
        Uri uri;
        if (url.toLowerCase(Locale.getDefault()).startsWith("http://")
                || url.toLowerCase(Locale.getDefault()).startsWith("https://")) {
            uri = Uri.parse(url);
        } else {
            uri = Uri.parse("http://" + url);
        }
        intent = new Intent(Intent.ACTION_VIEW, uri);
        iconRes = R.drawable.ic_open_url;
    }

    private CharSequence buildLabel(Context context, String url) {
        String beautifiedUrl = url.replaceFirst("https?://", "");
        return new SpannableStringBuilder(context.getText(R.string.hint_search_open_web))
                .append(' ')
                .append(beautifiedUrl, new UnderlineSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
