package com.italankin.lnch.feature.home.apps.delegate;

import android.view.View;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;

import androidx.annotation.Nullable;

public interface ShortcutStarterDelegate {

    void startShortcut(@Nullable Shortcut shortcut, @Nullable View view);
}
