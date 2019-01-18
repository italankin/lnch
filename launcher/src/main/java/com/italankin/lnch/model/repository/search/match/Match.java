package com.italankin.lnch.model.repository.search.match;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

public interface Match {

    Uri getIcon();

    @DrawableRes
    int getIconResource();

    CharSequence getLabel(Context context);

    @ColorInt
    int getColor(Context context);

    @Override
    String toString();

    Intent getIntent();

    Set<Action> availableActions();

    enum Action {
        PIN,
        INFO
    }
}
