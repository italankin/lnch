package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.EnumSet;

import androidx.annotation.Nullable;

public class IntentSearchDelegate extends AbstractSearchDelegate<IntentDescriptor> {

    public IntentSearchDelegate(DescriptorRepository descriptorRepository) {
        super(descriptorRepository, IntentDescriptor.class);
    }

    @Override
    boolean isTargetEnabled(EnumSet<Preferences.SearchTarget> searchTargets) {
        return searchTargets.contains(Preferences.SearchTarget.URL) ||
                searchTargets.contains(Preferences.SearchTarget.WEB);
    }

    @Nullable
    @Override
    PartialMatch testTarget(IntentDescriptor item, String query) {
        PartialMatch.Type matchType = DescriptorSearchUtils.test(item, query);
        if (matchType != null) {
            return new PartialDescriptorMatch(item, matchType);
        }
        return null;
    }
}
