package com.italankin.lnch.model.repository.usage;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;

import java.util.List;

public interface UsageTracker {

    void trackLaunch(Descriptor descriptor);

    void trackShortcut(Shortcut shortcut);

    List<Descriptor> getMostUsed();
}
