package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;

public class UrlMatch extends PartialMatch {

    private static final Set<Action> ACTIONS = Collections.singleton(Action.PIN);

    private final String url;

    public UrlMatch(String url) {
        super(Type.OTHER);
        this.url = url.replaceFirst("https?://", "");
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

    @Override
    public CharSequence getLabel(Context context) {
        if (label == null) {
            label = new SpannableStringBuilder(context.getText(R.string.hint_search_open_web))
                    .append(' ')
                    .append(url, new UnderlineSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return label;
    }

    @Override
    public Kind getKind() {
        return Kind.URL;
    }

    @NonNull
    @Override
    public String toString() {
        return url;
    }

    @Override
    public int hashCode() {
        return 2;
    }

    @Override
    public int getColor(Context context) {
        return ResUtils.resolveColor(context, R.attr.colorText);
    }
}
