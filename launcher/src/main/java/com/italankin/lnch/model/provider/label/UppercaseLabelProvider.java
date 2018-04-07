package com.italankin.lnch.model.provider.label;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Locale;

public class UppercaseLabelProvider extends LabelProviderImpl {
    @Override
    public String get(PackageManager pm, ResolveInfo ri) {
        return super.get(pm, ri).toUpperCase(Locale.getDefault());
    }
}
