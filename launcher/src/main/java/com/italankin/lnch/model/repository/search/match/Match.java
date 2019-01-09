package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

import java.util.Set;

public interface Match {

    Uri getIcon();

    @DrawableRes
    int getIconResource();

    CharSequence getLabel();

    @ColorInt
    int getColor();

    @Override
    String toString();

    Intent getIntent();

    Set<Action> availableActions();

    enum Action {
        START,
        PIN,
        INFO
    }
}
