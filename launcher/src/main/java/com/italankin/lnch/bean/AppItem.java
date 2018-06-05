package com.italankin.lnch.bean;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class AppItem {
    public static final Comparator<AppItem> CMP_NAME_ASC = new NameComparator(true);
    public static final Comparator<AppItem> CMP_NAME_DESC = new NameComparator(false);

    @SerializedName("id")
    public String id;

    @SerializedName("versionCode")
    public long versionCode;

    @SerializedName("componentName")
    public String componentName;

    @SerializedName("label")
    public String label;

    @SerializedName("customLabel")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("customColor")
    public Integer customColor;

    @SerializedName("hidden")
    public boolean hidden;

    @Keep
    public AppItem() {
    }

    public AppItem(String id) {
        this.id = id;
    }

    public String getLabel() {
        if (customLabel != null) {
            return customLabel;
        }
        return label;
    }

    public int getColor() {
        if (customColor != null) {
            return customColor;
        }
        return color;
    }

    @Override
    public String toString() {
        return "{id=" + id + ", hidden=" + hidden + "}";
    }
}

class NameComparator implements Comparator<AppItem> {
    private final boolean asc;

    NameComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(AppItem lhs, AppItem rhs) {
        int compare = String.CASE_INSENSITIVE_ORDER.compare(lhs.label, rhs.label);
        return asc ? compare : -compare;
    }
}
