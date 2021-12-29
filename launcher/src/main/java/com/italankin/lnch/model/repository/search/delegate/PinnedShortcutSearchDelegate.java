package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.EnumSet;

import androidx.annotation.Nullable;

public class PinnedShortcutSearchDelegate extends AbstractSearchDelegate<PinnedShortcutDescriptor> {

    public PinnedShortcutSearchDelegate(DescriptorRepository descriptorRepository) {
        super(descriptorRepository, PinnedShortcutDescriptor.class);
    }

    @Override
    boolean isTargetEnabled(EnumSet<Preferences.SearchTarget> searchTargets) {
        return searchTargets.contains(Preferences.SearchTarget.SHORTCUT);
    }

    @Nullable
    @Override
    PartialMatch testTarget(PinnedShortcutDescriptor item, String query) {
        PartialMatch.Type matchType = DescriptorSearchUtils.test(item, query);
        if (matchType != null) {
            return new PartialDescriptorMatch(item, matchType);
        }
        return null;
    }
}
