package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.R;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.IntentUtils;

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
        PartialDescriptorMatch match = DescriptorSearchUtils.test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = IntentUtils.fromUri(item.uri);
            match.iconRes = R.drawable.ic_shortcut;
        }
        return match;
    }
}
