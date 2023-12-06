package com.italankin.lnch.model.repository.search.delegate;

import android.content.pm.PackageManager;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.search.SearchUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AppSearchDelegate implements SearchDelegate {

    private final PackageManager packageManager;
    private final DescriptorRepository descriptorRepository;

    public AppSearchDelegate(PackageManager packageManager, DescriptorRepository descriptorRepository) {
        this.packageManager = packageManager;
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public List<Match> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        boolean skipIgnored = !searchTargets.contains(Preferences.SearchTarget.IGNORED);
        List<Match> matches = new ArrayList<>(4);
        for (AppDescriptor descriptor : descriptorRepository.itemsOfType(AppDescriptor.class)) {
            if (descriptor.ignored && skipIgnored || (descriptor.searchFlags & AppDescriptor.FLAG_SEARCH_VISIBLE) == 0) {
                continue;
            }
            Searchable.Match match = SearchUtils.match(descriptor, query);
            if (match != null) {
                matches.add(new PartialDescriptorMatch(descriptor, packageManager, PartialMatch.Type.fromSearchable(match)));
            }
        }
        return matches;
    }
}
