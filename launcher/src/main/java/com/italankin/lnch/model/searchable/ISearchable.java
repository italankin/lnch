package com.italankin.lnch.model.searchable;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import java.util.Locale;

public interface ISearchable {
    static boolean contains(String what, String substring) {
        return what != null && substring != null && what.toLowerCase(Locale.getDefault())
                .contains(substring.toLowerCase(Locale.getDefault()));
    }

    static boolean startsWith(String what, String prefix) {
        return what != null && prefix != null && what.toLowerCase(Locale.getDefault())
                .startsWith(prefix.toLowerCase(Locale.getDefault()));
    }

    @NonNull
    Match filter(String constraint);

    @ColorInt
    int getColor();

    CharSequence getLabel();

    enum Match {
        NONE,
        STARTS_WITH,
        CONTAINS,
        OTHER
    }
}

