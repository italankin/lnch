package com.italankin.lnch.model.repository.search.delegate;

import android.graphics.Color;
import android.os.Build;

import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.apps.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.picasso.PackageIconHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.italankin.lnch.util.SearchUtils.contains;

public class DeepShortcutSearchDelegate implements SearchDelegate {

    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;

    public DeepShortcutSearchDelegate(DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository) {
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
    }

    @Override
    public List<PartialMatch> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1
                || !searchTargets.contains(Preferences.SearchTarget.SHORTCUT)) {
            return Collections.emptyList();
        }
        List<PartialMatch> matches = new ArrayList<>(1);
        for (AppDescriptor descriptor : descriptorRepository.itemsOfType(AppDescriptor.class)) {
            List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(descriptor);
            if (shortcuts == null || shortcuts.isEmpty()) {
                continue;
            }
            for (Shortcut shortcut : shortcuts) {
                if (shortcut.isEnabled() && contains(shortcut.getShortLabel().toString(), query)) {
                    PartialMatch match = new PartialMatch(PartialMatch.Type.OTHER);
                    match.icon = PackageIconHandler.uriFrom(shortcut.getPackageName());
                    match.label = shortcut.getShortLabel();
                    match.color = Color.WHITE;
                    match.intent = StartShortcutReceiver.makeStartIntent(shortcut);
                    match.descriptor = descriptor;
                    matches.add(match);
                }
            }
        }
        return matches;
    }
}
