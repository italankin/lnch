package com.italankin.lnch.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public final class Typefaces {

    public static Typeface VARELA_ROUND;

    public static void init(AssetManager assetManager) {
        VARELA_ROUND = Typeface.createFromAsset(assetManager, "fonts/VarelaRound-Regular.ttf");
    }

}
