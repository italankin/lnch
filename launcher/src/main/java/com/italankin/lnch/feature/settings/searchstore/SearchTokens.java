package com.italankin.lnch.feature.settings.searchstore;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

interface SearchTokens {

    static SearchTokens resources(@StringRes Integer... resources) {
        return new ResourcesSearchTokens(resources);
    }

    static SearchTokens arrays(@ArrayRes Integer... arrays) {
        return new ArraysSearchTokens(arrays);
    }

    static SearchTokens strings(String... strings) {
        return new StringsSearchTokens(strings);
    }

    List<String> get(Context context);
}

class StringsSearchTokens implements SearchTokens {
    private final List<String> strings;

    StringsSearchTokens(String... strings) {
        this.strings = Collections.unmodifiableList(Arrays.asList(strings));
    }

    @Override
    public List<String> get(Context context) {
        return this.strings;
    }
}

class ResourcesSearchTokens implements SearchTokens {
    private final List<Integer> resources;

    ResourcesSearchTokens(@StringRes Integer... resources) {
        this.resources = Collections.unmodifiableList(Arrays.asList(resources));
    }

    @Override
    public List<String> get(Context context) {
        List<String> result = new ArrayList<>(resources.size());
        for (Integer resource : resources) {
            result.add(context.getString(resource));
        }
        return result;
    }
}

class ArraysSearchTokens implements SearchTokens {
    private final List<Integer> arrays;

    ArraysSearchTokens(@ArrayRes Integer... resources) {
        this.arrays = Collections.unmodifiableList(Arrays.asList(resources));
    }

    @Override
    public List<String> get(Context context) {
        List<String> result = new ArrayList<>(arrays.size());
        for (Integer array : arrays) {
            String[] stringArray = context.getResources().getStringArray(array);
            result.addAll(Arrays.asList(stringArray));
        }
        return result;
    }
}
