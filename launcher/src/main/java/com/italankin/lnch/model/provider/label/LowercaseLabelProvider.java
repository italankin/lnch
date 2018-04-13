package com.italankin.lnch.model.provider.label;

import android.content.pm.LauncherActivityInfo;

import java.util.Locale;

public class LowercaseLabelProvider extends LabelProviderImpl {
    @Override
    public String get(LauncherActivityInfo info) {
        return super.get(info).toLowerCase(Locale.getDefault());
    }
}
