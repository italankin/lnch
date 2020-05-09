package com.italankin.lnch.api;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class LauncherIntents {

    public static final String ACTION_EDIT_MODE = "com.italankin.lnch.action.EDIT_MODE";

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    public static final String ACTION_START_SHORTCUT = "com.italankin.lnch.action.START_SHORTCUT";
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    public static final String EXTRA_SHORTCUT_ID = "shortcut_id";
}
