package com.italankin.lnch.model.repository.search.match;

import android.content.Intent;
import android.net.Uri;

import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

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
        PIN,
        INFO
    }
}
