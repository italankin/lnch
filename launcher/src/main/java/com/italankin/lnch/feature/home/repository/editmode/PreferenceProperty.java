package com.italankin.lnch.feature.home.repository.editmode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.model.repository.prefs.Preferences;

class PreferenceProperty<T> implements EditModeState.PreferenceProperty<T> {
    private final Preferences.Pref<T> pref;

    PreferenceProperty(Preferences.Pref<T> pref) {
        this.pref = pref;
    }

    @Override
    public void write(Preferences preferences, T newValue) {
        if (newValue != null) {
            preferences.set(pref, newValue);
        } else {
            preferences.reset(pref);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PreferenceProperty) {
            return pref.equals(((PreferenceProperty<?>) obj).pref);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pref.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return pref.key();
    }
}
