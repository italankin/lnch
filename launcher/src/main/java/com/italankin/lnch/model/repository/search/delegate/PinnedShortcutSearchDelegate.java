package com.italankin.lnch.model.repository.search.delegate;

import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.search.SearchUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.EnumSet;

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
        Searchable.Match match = SearchUtils.match(item, query);
        if (match != null) {
            return new PartialDescriptorMatch(item, PartialMatch.Type.fromSearchable(match));
        }
        return null;
    }
}
