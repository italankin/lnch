package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.content.Intent;

import com.italankin.lnch.api.LauncherIntents;

public class CustomizeDelegate {

    private final Context context;

    public CustomizeDelegate(Context context) {
        this.context = context;
    }

    public void startCustomize() {
        context.startActivity(new Intent(LauncherIntents.ACTION_EDIT_MODE));
    }
}
