package com.italankin.lnch.model.backup;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import timber.log.Timber;

@SuppressWarnings("unchecked")
public class PreferencesBackupImpl implements PreferencesBackup {

    private final SharedPreferences sharedPreferences;

    public PreferencesBackupImpl(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Map<String, Object> read() {
        // cast to Object here so Gson will not convert values to Strings
        return (Map<String, Object>) sharedPreferences.getAll();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void write(Map<String, ?> map) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Preferences.Pref<?> pref : Preferences.ALL) {
            String key = pref.key();
            Object value = map.get(key);
            if (value == null) {
                editor.remove(key);
                continue;
            }
            Class<?> klass = value.getClass();
            if (klass.isAssignableFrom(int.class) || klass.isAssignableFrom(Integer.class)) {
                editor.putInt(key, (Integer) value);
            } else if (klass.isAssignableFrom(float.class) || klass.isAssignableFrom(Float.class)) {
                editor.putFloat(key, (Float) value);
            } else if (klass.isAssignableFrom(boolean.class) || klass.isAssignableFrom(Boolean.class)) {
                editor.putBoolean(key, (Boolean) value);
            } else if (klass.isAssignableFrom(String.class)) {
                editor.putString(key, (String) value);
            } else if (value instanceof Collection) {
                editor.putStringSet(key, new HashSet<>((Collection<? extends String>) value));
            }
        }
        editor.commit();
        Timber.d("wrote %d keys: %s", map.size(), map);
    }
}
