package com.italankin.lnch.feature.intentfactory.category;

import android.content.Intent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class IntentCategory {
    private static IntentCategory[] CATEGORIES;

    public static IntentCategory[] getAll() {
        if (CATEGORIES != null) {
            return CATEGORIES;
        }
        List<IntentCategory> categories = new ArrayList<>();
        for (Field field : Intent.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                if (!name.startsWith("CATEGORY_")) {
                    continue;
                }
                try {
                    String value = (String) field.get(null);
                    categories.add(new IntentCategory(name, value));
                } catch (IllegalAccessException ignored) {
                    // probably, we should not access that
                }
            }
        }
        CATEGORIES = categories.toArray(new IntentCategory[0]);
        return CATEGORIES;
    }

    public final String name;
    public final String value;

    IntentCategory(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
