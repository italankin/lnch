package com.italankin.lnch.model.repository.search.delegate;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
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
        List<PartialMatch> result = new ArrayList<>(4);
        for (ShortcutData data : getAllShortcuts()) {
            if (contains(data.shortcut.getShortLabel().toString(), query)) {
                PartialMatch match = createMatch(data.shortcut, data.descriptor);
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
                List<Shortcut> shortcuts = shortcutsRepository.getShortcuts((AppDescriptor) descriptor);
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
    private static PartialMatch createMatch(Shortcut shortcut, Descriptor descriptor) {
        PartialMatch match = new PartialMatch(PartialMatch.Type.OTHER);
        match.icon = ShortcutIconHandler.uriFrom(shortcut, true);
        match.label = shortcut.getShortLabel();
        match.color = Color.WHITE;
        match.intent = StartShortcutReceiver.makeStartIntent(shortcut);
        match.descriptor = descriptor;
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
