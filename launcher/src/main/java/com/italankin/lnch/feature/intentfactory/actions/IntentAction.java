package com.italankin.lnch.feature.intentfactory.actions;

import android.content.Intent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class IntentAction {
    private static IntentAction[] ACTIONS;

    public static IntentAction[] getAll() {
        if (ACTIONS != null) {
            return ACTIONS;
        }
        List<IntentAction> actions = new ArrayList<>();
        for (Field field : Intent.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                if (!name.startsWith("ACTION_")) {
                    continue;
                }
                try {
                    String value = (String) field.get(null);
                    actions.add(new IntentAction(name, value));
                } catch (IllegalAccessException ignored) {
                    // probably, we should not access that
                }
            }
        }
        return ACTIONS = actions.toArray(new IntentAction[0]);
    }

    public final String name;
    public final String value;

    IntentAction(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
