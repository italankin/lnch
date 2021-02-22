package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

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

    @Override
    String toString();

    /**
     * @return an intent, if present
     */
    Intent getIntent();

    Set<Action> availableActions();

    enum Action {
        PIN,
        INFO
    }
}
