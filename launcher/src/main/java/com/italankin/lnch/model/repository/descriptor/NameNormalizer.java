package com.italankin.lnch.model.repository.descriptor;

import java.util.Locale;

public class NameNormalizer {

    public String normalize(CharSequence s) {
        return s.toString()
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.getDefault());
    }
}
