package com.italankin.lnch.model.repository.search.delegate;

import android.os.Build;

import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.picasso.ShortcutIconHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.RequiresApi;

import static com.italankin.lnch.util.SearchUtils.contains;

public class DeepShortcutSearchDelegate implements SearchDelegate {

    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;

    public DeepShortcutSearchDelegate(DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository) {
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
    }

    @Override
    public List<Match> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1
                || !searchTargets.contains(Preferences.SearchTarget.SHORTCUT)) {
            return Collections.emptyList();
        }
        List<Match> result = new ArrayList<>(4);
        for (ShortcutData data : getAllShortcuts()) {
            if (contains(data.shortcut.getShortLabel().toString(), query)) {
                Match match = createMatch(data.shortcut, data.descriptor);
                result.add(match);
            }
        }
        return result;
    }

    private Set<ShortcutData> getAllShortcuts() {
        List<Descriptor> descriptors = descriptorRepository.items();
        Set<ShortcutData> result = new LinkedHashSet<>(descriptors.size() * 2);
        for (Descriptor descriptor : descriptors) {
            if (descriptor instanceof AppDescriptor) {
                AppDescriptor appDescriptor = (AppDescriptor) descriptor;
                if ((appDescriptor.searchFlags & AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE) == 0) {
                    continue;
                }
                List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(appDescriptor);
                if (shortcuts == null || shortcuts.isEmpty()) {
                    continue;
                }
                for (Shortcut shortcut : shortcuts) {
                    if (shortcut.isEnabled()) {
                        result.add(new ShortcutData(shortcut, descriptor));
                    }
                }
            } else if (descriptor instanceof DeepShortcutDescriptor) {
                DeepShortcutDescriptor dsd = (DeepShortcutDescriptor) descriptor;
                Shortcut shortcut = shortcutsRepository.getShortcut(dsd.packageName, dsd.id);
                if (shortcut != null && shortcut.isEnabled()) {
                    result.add(new ShortcutData(shortcut, dsd));
                }
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private static Match createMatch(Shortcut shortcut, Descriptor descriptor) {
        PartialDescriptorMatch match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.OTHER, Match.Kind.SHORTCUT);
        match.icon = ShortcutIconHandler.uriFrom(shortcut, true);
        match.label = shortcut.getShortLabel();
        match.intent = StartShortcutReceiver.makeStartIntent(shortcut);
        return match;
    }

    private static class ShortcutData {
        final Shortcut shortcut;
        final Descriptor descriptor;

        ShortcutData(Shortcut shortcut, Descriptor descriptor) {
            this.shortcut = shortcut;
            this.descriptor = descriptor;
        }

        @Override
        public int hashCode() {
            int result = shortcut.getPackageName().hashCode();
            result = 31 * result + shortcut.getId().hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof ShortcutData) {
                ShortcutData that = (ShortcutData) obj;
                return this.shortcut.getPackageName().equals(that.shortcut.getPackageName())
                        && this.shortcut.getId().equals(that.shortcut.getId());
            }
            return false;
        }
    }
}
