package com.italankin.lnch.bean;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class AppItem_v1 implements Comparable<AppItem_v1> {

    @SerializedName("packageName")
    public String packageName;

    @SerializedName("versionCode")
    public int versionCode;

    @SerializedName("label")
    public String label;

    @SerializedName("customLabel")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("customColor")
    public Integer customColor;

    @SerializedName("order")
    public int order;

    @SerializedName("hidden")
    public boolean hidden;

    public AppItem_v1() {
    }

    public AppItem toAppItem() {
        AppItem item = new AppItem(packageName);
        item.versionCode = versionCode;
        item.label = label;
        item.customLabel = customLabel;
        item.color = color;
        item.customColor = customColor;
        item.hidden = hidden;
        return item;
    }

    @Override
    public int compareTo(@NonNull AppItem_v1 another) {
        return Integer.compare(order, another.order);
    }
}

