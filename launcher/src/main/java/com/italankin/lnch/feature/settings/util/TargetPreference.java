package com.italankin.lnch.feature.settings.util;

import android.content.Context;
import android.os.Bundle;

import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TargetPreference {

    public static final int HIGHLIGHT_DELAY = 500;
    public static final int HIGHLIGHT_DURATION = 500;

    private static final String KEY_TARGET_PREFERENCE = "target_preference";

    public static Fragment set(Context context, Fragment fragment, SettingsEntry.Key key) {
        Bundle arguments = fragment.getArguments();
        if (arguments == null) {
            arguments = new Bundle(1);
            fragment.setArguments(arguments);
        }
        String targetPreference;
        if (key instanceof SettingsEntry.StringKey) {
            targetPreference = ((SettingsEntry.StringKey) key).value;
        } else if (key instanceof SettingsEntry.ResourceKey) {
            targetPreference = context.getString(((SettingsEntry.ResourceKey) key).value);
        } else {
            throw new IllegalArgumentException("Unknown key type: " + key);
        }
        arguments.putString(KEY_TARGET_PREFERENCE, targetPreference);
        return fragment;
    }

    @Nullable
    public static String get(Fragment fragment) {
        Bundle arguments = fragment.getArguments();
        if (arguments != null) {
            String key = arguments.getString(KEY_TARGET_PREFERENCE);
            arguments.remove(KEY_TARGET_PREFERENCE);
            return key;
        } else {
            return null;
        }
    }
}
