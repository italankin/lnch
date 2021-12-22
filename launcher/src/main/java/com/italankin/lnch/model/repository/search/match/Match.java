package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public interface Match {

    /**
     * @return uri of match icon
     */
    Uri getIcon();

    /**
     * @return drawable resource of the match icon
     */
    @DrawableRes
    int getIconResource();

    CharSequence getLabel(Context context);

    @ColorInt
    int getColor(Context context);

    @NonNull
    @Override
    String toString();

    /**
     * @return an intent, if present
     */
    Intent getIntent();

    /**
     * @return kind of a match, e.g. application, shortcut, web result, etc.
     */
    Kind getKind();

    Set<Action> availableActions();

    /**
     * Kind of the object this match represents
     */
    enum Kind {
        APP,
        SHORTCUT,
        OTHER,
        WEB,
        URL
    }

    enum Action {
        PIN,
        INFO
    }
}
