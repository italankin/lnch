package com.italankin.lnch.model.repository.search.delegate;

import android.content.Intent;

import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.IntentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class IntentSearchDelegate implements SearchDelegate {

    private final DescriptorRepository descriptorRepository;

    public IntentSearchDelegate(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public List<PartialMatch> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (!searchTargets.contains(Preferences.SearchTarget.URL) &&
                !searchTargets.contains(Preferences.SearchTarget.WEB)) {
            return Collections.emptyList();
        }
        List<PartialMatch> matches = new ArrayList<>(2);
        for (IntentDescriptor descriptor : descriptorRepository.itemsOfType(IntentDescriptor.class)) {
            PartialMatch match = testIntent(descriptor, query);
            if (match != null) {
                matches.add(match);
            }
        }
        return matches;
    }

    private static PartialMatch testIntent(IntentDescriptor item, String query) {
        PartialMatch match = DescriptorSearchUtils.test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = IntentUtils.fromUri(item.intentUri, Intent.URI_INTENT_SCHEME);
            match.iconRes = 0; // TODO
            match.descriptor = item;
        }
        return match;
    }
}
