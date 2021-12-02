package com.italankin.lnch.feature.intentfactory.extras;

import android.os.Bundle;

import java.util.List;

import timber.log.Timber;

class IntentExtra {

    static void putAllFrom(Bundle bundle, List<IntentExtra> out) {
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Class<?> klass = value.getClass();
            if (klass.isAssignableFrom(int.class) || klass.isAssignableFrom(Integer.class)) {
                out.add(new IntentExtra(IntentExtra.Type.INT, key, value));
            } else if (klass.isAssignableFrom(float.class) || klass.isAssignableFrom(Float.class)) {
                out.add(new IntentExtra(IntentExtra.Type.FLOAT, key, value));
            } else if (klass.isAssignableFrom(boolean.class) || klass.isAssignableFrom(Boolean.class)) {
                out.add(new IntentExtra(IntentExtra.Type.BOOLEAN, key, value));
            } else if (klass.isAssignableFrom(String.class)) {
                out.add(new IntentExtra(IntentExtra.Type.STRING, key, value));
            } else {
                Timber.w("Cannot convert object for key '%s' of type '%s' to IntentExtra ", key, klass);
            }
        }
    }

    final Type type;
    final String key;
    final Object value;

    IntentExtra(Type type, String key, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public void putTo(Bundle bundle) {
        switch (type) {
            case INT:
                bundle.putInt(key, (int) value);
                break;
            case STRING:
                bundle.putString(key, (String) value);
                break;
            case FLOAT:
                bundle.putFloat(key, (float) value);
                break;
            case BOOLEAN:
                bundle.putBoolean(key, (boolean) value);
                break;
        }
    }

    enum Type {
        STRING("String"),
        INT("int"),
        FLOAT("float"),
        BOOLEAN("boolean");

        final String name;

        Type(String name) {
            this.name = name;
        }

        Object convertValue(String value) {
            switch (this) {
                case INT:
                    return Integer.parseInt(value);
                case STRING:
                    return value;
                case FLOAT:
                    return Float.parseFloat(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                default:
                    throw new IllegalArgumentException("Cannot convert String to " + this.name());
            }
        }
    }
}
