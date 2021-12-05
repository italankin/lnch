package com.italankin.lnch.feature.intentfactory.flags;

import android.content.Intent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class IntentFlag {
    public static final int DEFAULT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK;

    private static IntentFlag[] FLAGS;

    public static IntentFlag[] getAll() {
        if (FLAGS != null) {
            return FLAGS;
        }
        List<IntentFlag> flags = new ArrayList<>();
        for (Field field : Intent.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                if (!name.startsWith("FLAG_ACTIVITY_")) {
                    continue;
                }
                try {
                    int value = field.getInt(null);
                    flags.add(new IntentFlag(name, value));
                } catch (IllegalAccessException ignored) {
                    // probably, we should not access that
                }
            }
        }
        FLAGS = flags.toArray(new IntentFlag[0]);
        return FLAGS;
    }

    public final String name;
    public final int value;

    IntentFlag(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
