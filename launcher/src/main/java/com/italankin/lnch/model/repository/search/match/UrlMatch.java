package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import com.italankin.lnch.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class UrlMatch extends PartialMatch {

    private static final Set<Action> ACTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Action.PIN, Action.START)));

    public UrlMatch(String url) {
        super(Type.OTHER);
        color = Color.WHITE;
        label = buildLabel(url);
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

    @Override
    public Set<Action> availableActions() {
        return ACTIONS;
    }

    private CharSequence buildLabel(String url) {
        String beautifiedUrl = url.replaceFirst("https?://", "");
        return new SpannableStringBuilder("Go to ")
                .append(beautifiedUrl, new UnderlineSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
