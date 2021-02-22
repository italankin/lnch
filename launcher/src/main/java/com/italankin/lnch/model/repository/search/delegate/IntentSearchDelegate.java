package com.italankin.lnch.model.repository.search.delegate;

import android.content.Intent;

import com.italankin.lnch.R;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.IntentUtils;

import java.util.Collections;
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
        PartialDescriptorMatch match = DescriptorSearchUtils.test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = IntentUtils.fromUri(item.intentUri, Intent.URI_INTENT_SCHEME);
            match.iconRes = R.drawable.ic_launch_intent;
            match.actions = Collections.emptySet();
        }
        return match;
    }
}
