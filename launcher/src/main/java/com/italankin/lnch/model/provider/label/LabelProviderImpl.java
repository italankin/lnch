package com.italankin.lnch.model.provider.label;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class LabelProviderImpl implements LabelProvider {
    @Override
    public String get(PackageManager pm, ResolveInfo ri) {
        return ri.loadLabel(pm).toString();
    }
}
