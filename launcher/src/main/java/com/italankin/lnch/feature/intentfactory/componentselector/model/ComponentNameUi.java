package com.italankin.lnch.feature.intentfactory.componentselector.model;

import android.content.ComponentName;
import android.net.Uri;

public class ComponentNameUi {
    public final String packageName;
    public final String className;
    public final ComponentName componentName;
    public final Uri iconUri;

    public ComponentNameUi(String packageName, String className, ComponentName componentName, Uri iconUri) {
        this.packageName = packageName;
        this.className = className;
        this.componentName = componentName;
        this.iconUri = iconUri;
    }
}
