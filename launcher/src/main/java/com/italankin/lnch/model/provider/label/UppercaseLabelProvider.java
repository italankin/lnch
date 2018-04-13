package com.italankin.lnch.model.provider.label;

import android.content.pm.LauncherActivityInfo;

import java.util.Locale;

public class UppercaseLabelProvider extends LabelProviderImpl {
    @Override
    public String get(LauncherActivityInfo info) {
        return super.get(info).toUpperCase(Locale.getDefault());
    }
}
