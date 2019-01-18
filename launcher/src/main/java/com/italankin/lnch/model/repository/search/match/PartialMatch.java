package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

public abstract class PartialMatch implements Match, Comparable<PartialMatch> {
    public final PartialMatch.Type type;
    public Uri icon;
    public int iconRes;
    public Integer color;
    public CharSequence label;
    public Intent intent;
    public Set<Action> actions = new HashSet<>(1);

    public PartialMatch(PartialMatch.Type type) {
        this.type = type;
    }

    @Override
    public Uri getIcon() {
        return icon;
    }

    @Override
    public int getIconResource() {
        return iconRes;
    }

    @Override
    public CharSequence getLabel() {
        return label;
    }

    @Override
    public int getColor(Context context) {
        return color != null ? color : ResUtils.resolveColor(context, R.attr.colorText);
    }

    @Override
    public String toString() {
        return label.toString();
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    @Override
    public Set<Action> availableActions() {
        return actions;
    }

    @Override
    public int compareTo(@NonNull PartialMatch match) {
        return type.compareTo(match.type);
    }

    public enum Type {
        EXACT,
        STARTS_WITH,
        CONTAINS_WORD,
        CONTAINS,
        OTHER,
    }
}
