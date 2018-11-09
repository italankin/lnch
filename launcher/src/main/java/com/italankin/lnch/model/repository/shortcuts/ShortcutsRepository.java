package com.italankin.lnch.model.repository.shortcuts;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.List;

import io.reactivex.Completable;

public interface ShortcutsRepository {

    Completable loadShortcuts();

    List<Shortcut> getShortcuts(AppDescriptor descriptor);

    Completable loadShortcuts(AppDescriptor descriptor);

    Shortcut getShortcut(String packageName, String shortcutId);

    Completable pinShortcut(Shortcut shortcut);

    void unpinShortcut(String packageName, String shortcutId);

    List<Shortcut> getPinnedShortcuts();

    interface DescriptorProvider {
        List<Descriptor> getDescriptors();
    }
}