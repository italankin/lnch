package com.italankin.lnch.model.searchable;

import android.graphics.Color;
import android.support.annotation.NonNull;

public class GoogleSearchable implements ISearchable {
    private String label;
    private String constraint;

    @NonNull
    @Override
    public Match filter(String constraint) {
        this.constraint = constraint;
        label = "Google: " + constraint;
        return Match.OTHER;
    }

    @Override
    public int getColor() {
        return Color.WHITE;
    }

    @Override
    public CharSequence getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return constraint;
    }
}
