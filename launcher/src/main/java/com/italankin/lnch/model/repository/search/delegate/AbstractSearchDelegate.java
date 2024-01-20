package com.italankin.lnch.model.repository.search.delegate;

import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public abstract class AbstractSearchDelegate<D extends Descriptor> implements SearchDelegate {

    private final DescriptorRepository descriptorRepository;
    private final Class<D> classOfDescriptor;

    AbstractSearchDelegate(DescriptorRepository descriptorRepository, Class<D> classOfDescriptor) {
        this.descriptorRepository = descriptorRepository;
        this.classOfDescriptor = classOfDescriptor;
    }

    @Override
    public List<Match> search(CharSequence constraint, String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (!isTargetEnabled(searchTargets)) {
            return Collections.emptyList();
        }
        List<D> items = descriptorRepository.itemsOfType(classOfDescriptor);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Match> results = new ArrayList<>(2);
        for (D item : items) {
            Match match = testTarget(item, query);
            if (match != null) {
                results.add(match);
            }
        }
        return results;
    }

    boolean isTargetEnabled(EnumSet<Preferences.SearchTarget> searchTargets) {
        return true;
    }

    @Nullable
    abstract Match testTarget(D descriptor, String query);
}
