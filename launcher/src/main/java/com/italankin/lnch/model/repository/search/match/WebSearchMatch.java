package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;

import java.util.Collections;
import java.util.Set;

public class WebSearchMatch extends PartialMatch {

    private static final Set<Action> ACTIONS = Collections.singleton(Action.PIN);

    public WebSearchMatch(String label, Uri uri) {
        super(Type.OTHER);
        this.label = label.trim();
        iconRes = R.drawable.ic_search;
        intent = new Intent(Intent.ACTION_VIEW, uri);
        actions = ACTIONS;
    }

    @Override
    public int getColor(Context context) {
        return ResUtils.resolveColor(context, R.attr.colorText);
    }

    @Override
    public Kind getKind() {
        return Kind.WEB;
    }
}
