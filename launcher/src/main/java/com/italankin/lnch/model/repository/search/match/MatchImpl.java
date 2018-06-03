package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class MatchImpl implements Match, Comparable<MatchImpl> {
    public final MatchImpl.Type type;
    public Drawable icon;
    public int iconRes;
    public int color;
    public CharSequence label;
    public Intent intent;

    public MatchImpl(MatchImpl.Type type) {
        this.type = type;
    }

    @Override
    public Drawable getIcon() {
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
    public int getColor() {
        return color;
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
    public int compareTo(@NonNull MatchImpl match) {
        return type.compareTo(match.type);
    }

    public enum Type {
        STARTS_WITH,
        CONTAINS_WORD,
        CONTAINS,
        OTHER,
    }
}
