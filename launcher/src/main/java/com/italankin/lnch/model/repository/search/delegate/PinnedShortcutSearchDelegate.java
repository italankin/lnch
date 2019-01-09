package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.R;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.IntentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class PinnedShortcutSearchDelegate implements SearchDelegate {

    private final DescriptorRepository descriptorRepository;

    public PinnedShortcutSearchDelegate(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public List<PartialMatch> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (!searchTargets.contains(Preferences.SearchTarget.SHORTCUT)) {
            return Collections.emptyList();
        }
        List<PartialMatch> matches = new ArrayList<>(2);
        for (PinnedShortcutDescriptor descriptor : descriptorRepository.itemsOfType(PinnedShortcutDescriptor.class)) {
            PartialMatch match = testShortcut(descriptor, query);
            if (match != null) {
                matches.add(match);
            }
        }
        return matches;
    }

    private static PartialMatch testShortcut(PinnedShortcutDescriptor item, String query) {
        PartialDescriptorMatch match = DescriptorSearchUtils.test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = IntentUtils.fromUri(item.uri);
            match.iconRes = R.drawable.ic_shortcut;
            match.descriptor = item;
        }
        return match;
    }
}
