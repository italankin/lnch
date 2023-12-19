package com.italankin.lnch.model.backup;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import com.italankin.lnch.model.repository.prefs.Preferences;
import timber.log.Timber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

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
        int skipped = 0;
        for (Preferences.Pref<?> pref : Preferences.ALL) {
            String key = pref.key();
            Object value = map.get(key);
            if (value == null) {
                editor.remove(key);
                Timber.d("remove key=%s", key);
                continue;
            }
            try {
                Class<?> prefType = pref.valueType();
                if (value instanceof Number) {
                    Number numberValue = (Number) value;
                    if (Integer.class.isAssignableFrom(prefType)) {
                        editor.putInt(key, numberValue.intValue());
                    } else if (Float.class.isAssignableFrom(prefType)) {
                        editor.putFloat(key, numberValue.floatValue());
                    }
                } else if (Boolean.class.isAssignableFrom(prefType) && value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (String.class.isAssignableFrom(prefType) && value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (Collection.class.isAssignableFrom(prefType) && value instanceof Collection) {
                    editor.putStringSet(key, new HashSet<>((Collection<? extends String>) value));
                } else {
                    skipped++;
                    Timber.w("mismatch type: key=%s, prefType=%s, value=%s, valueType=%s", key, prefType, value, value.getClass());
                    continue;
                }
                Timber.d("write %s=%s", key, value);
            } catch (Exception e) {
                skipped++;
                Timber.e(e, "error processing key=%s, value=%s", key, value);
            }
        }
        editor.commit();
        Timber.d("wrote %d keys, %s skipped", map.size() - skipped, skipped);
    }
}
