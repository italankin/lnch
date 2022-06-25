package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.Set;

public interface Match {

    /**
     * @return uri of match icon
     */
    Uri getIcon();

    /**
     * @return a drawable resource icon
     */
    Drawable getDrawableIcon(Context context);

    CharSequence getLabel(Context context);

    @ColorInt
    int getColor(Context context);

    @NonNull
    @Override
    String toString();

    /**
     * @return an intent, if present
     */
    Intent getIntent(Context context);

    /**
     * @return kind of a match, e.g. application, shortcut, web result, etc.
     */
    Kind getKind();

    Set<Action> availableActions();

    @Override
    int hashCode();

    /**
     * Kind of the object this match represents
     */
    enum Kind {
        APP,
        SHORTCUT,
        PREFERENCE,
        OTHER,
        WEB,
        URL
    }

    enum Action {
        PIN,
        INFO
    }
}
