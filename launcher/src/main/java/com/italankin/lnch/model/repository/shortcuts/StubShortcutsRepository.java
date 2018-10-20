package com.italankin.lnch.model.repository.shortcuts;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;

public class StubShortcutsRepository implements ShortcutsRepository {
    @Override
    public Completable loadShortcuts() {
        return Completable.complete();
    }

    @Override
    public List<Shortcut> getShortcuts(AppDescriptor descriptor) {
        return Collections.emptyList();
    }

    @Override
    public Completable loadShortcuts(AppDescriptor descriptor) {
        return Completable.complete();
    }

    @Override
    public Shortcut getShortcut(String packageName, String shortcutId) {
        return null;
    }
}
