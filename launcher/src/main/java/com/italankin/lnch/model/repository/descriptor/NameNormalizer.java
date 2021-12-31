package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.Locale;

public class NameNormalizer {

    private final Preferences preferences;

    public NameNormalizer(Preferences preferences) {
        this.preferences = preferences;
    }

    public String normalize(CharSequence s) {
        String label = s.toString()
                .replaceAll("\\s+", " ")
                .trim();
        switch (preferences.get(Preferences.NAME_TRANSFORM)) {
            case LOWER:
                return label.toLowerCase(Locale.getDefault());
            case UPPER:
                return label.toUpperCase(Locale.getDefault());
            default:
            case AS_IS:
                return label;
        }
    }
}
