package com.italankin.lnch.model.provider.label;

import android.content.pm.LauncherActivityInfo;

public class LabelProviderImpl implements LabelProvider {
    @Override
    public String get(LauncherActivityInfo info) {
        return info.getLabel()
                .toString()
                .replaceAll("\\s+", " ")
                .trim();
    }
}
