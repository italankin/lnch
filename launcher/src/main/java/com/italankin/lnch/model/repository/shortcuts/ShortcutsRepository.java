package com.italankin.lnch.model.repository.shortcuts;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface ShortcutsRepository {

    Completable loadShortcuts();

    List<Shortcut> getShortcuts(AppDescriptor descriptor);

    Completable loadShortcuts(AppDescriptor descriptor);

    Shortcut getShortcut(String packageName, String shortcutId);

    Single<Boolean> pinShortcut(Shortcut shortcut);

    Single<Boolean> pinShortcut(String packageName, String shortcutId);
}
