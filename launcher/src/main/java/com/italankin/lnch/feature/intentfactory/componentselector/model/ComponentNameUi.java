package com.italankin.lnch.feature.intentfactory.componentselector.model;

import android.content.ComponentName;
import android.net.Uri;
import com.italankin.lnch.util.search.Searchable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ComponentNameUi implements Searchable {
    public final String packageName;
    public final String className;
    public final ComponentName componentName;
    public final Uri iconUri;
    private final Set<String> searchTokens = new HashSet<>(2);

    public ComponentNameUi(String packageName, String className, ComponentName componentName, Uri iconUri) {
        this.packageName = packageName;
        this.className = className;
        this.componentName = componentName;
        this.iconUri = iconUri;
        Collections.addAll(searchTokens, packageName, className);
    }

    @Override
    public Set<String> getSearchTokens() {
        return searchTokens;
    }
}
