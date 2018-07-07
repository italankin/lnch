package com.italankin.lnch.bean;

import android.graphics.Color;

public class GroupSeparator extends AppItem {

    public static final String ID = "com.italankin.lnch.separator";

    public GroupSeparator() {
        super(ID);
        label = "New Group";
        color = Color.WHITE;
        versionCode = System.nanoTime();
    }
}
