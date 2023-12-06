package com.italankin.lnch.model.repository.search.delegate;

import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.search.SearchUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.EnumSet;

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
        Searchable.Match match = SearchUtils.match(item, query);
        if (match != null) {
            return new PartialDescriptorMatch(item, PartialMatch.Type.fromSearchable(match));
        }
        return null;
    }
}
