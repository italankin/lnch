package com.italankin.lnch.model.repository.search;

import android.content.pm.PackageManager;
import android.os.Build;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.util.DescriptorUtils;
import timber.log.Timber;

import java.util.*;

import static com.italankin.lnch.model.repository.prefs.Preferences.SEARCH_SHOW_MOST_USED;
import static com.italankin.lnch.model.repository.prefs.Preferences.SearchTarget;

public class SearchRepositoryImpl implements SearchRepository {

    private static final int MAX_RESULTS = 4;
    private static final int MAX_RESULTS_RECENT = 5;
    private static final Comparator<Match> MATCH_COMPARATOR = new MatchComparator();

    private final PackageManager packageManager;
    private final List<SearchDelegate> delegates;
    private final List<SearchDelegate> additionalDelegates;
    private final Preferences preferences;
    private final UsageTracker usageTracker;
    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;

    /**
     * @param delegates           search delegates
     * @param additionalDelegates additional search delegates, which can add their results above {@link #MAX_RESULTS} limit
     */
    public SearchRepositoryImpl(
            PackageManager packageManager,
            List<SearchDelegate> delegates,
            List<SearchDelegate> additionalDelegates,
            Preferences preferences,
            UsageTracker usageTracker,
            DescriptorRepository descriptorRepository,
            ShortcutsRepository shortcutsRepository) {
        this.packageManager = packageManager;
        this.delegates = delegates;
        this.additionalDelegates = additionalDelegates;
        this.preferences = preferences;
        this.usageTracker = usageTracker;
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
    }

    @Override
    public List<? extends Match> search(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return Collections.emptyList();
        }
        String query = constraint.toString().trim().toLowerCase(Locale.getDefault());
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        long start = System.nanoTime();
        EnumSet<SearchTarget> searchTargets = EnumSet.copyOf(SearchTarget.ALL);
        EnumSet<SearchTarget> excludedSearchTargets = preferences.get(Preferences.EXCLUDED_SEARCH_TARGETS);
        searchTargets.removeAll(excludedSearchTargets);
        List<Match> matches = new ArrayList<>(8);
        for (SearchDelegate delegate : delegates) {
            List<Match> list = delegate.search(query, searchTargets);
            matches.addAll(list);
        }
        if (matches.size() > 1) {
            Collections.sort(matches, MATCH_COMPARATOR);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }
        for (SearchDelegate delegate : additionalDelegates) {
            List<Match> list = delegate.search(query, searchTargets);
            matches.addAll(list);
        }
        Timber.d("search: query='%s', results=%d, done in %.3fms",
                query, matches.size(), (System.nanoTime() - start) / 1_000_000f);
        return matches;
    }

    @Override
    public List<? extends Match> recent() {
        if (!preferences.get(SEARCH_SHOW_MOST_USED)) {
            return Collections.emptyList();
        }
        List<String> descriptors = usageTracker.getMostUsed();
        Map<String, Descriptor> descriptorMap = DescriptorUtils.associateById(descriptorRepository.items());
        List<Match> matches = new ArrayList<>(descriptors.size());
        int count = 0;
        for (String descriptorId : descriptors) {
            Descriptor descriptor = descriptorMap.get(descriptorId);
            if (descriptor == null) {
                continue;
            }
            PartialDescriptorMatch match;
            if (descriptor instanceof AppDescriptor) {
                match = new PartialDescriptorMatch((AppDescriptor) descriptor, packageManager, PartialMatch.Type.EXACT);
            } else if (descriptor instanceof IntentDescriptor) {
                match = new PartialDescriptorMatch((IntentDescriptor) descriptor, PartialMatch.Type.EXACT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && descriptor instanceof DeepShortcutDescriptor) {
                DeepShortcutDescriptor d = (DeepShortcutDescriptor) descriptor;
                Shortcut shortcut = shortcutsRepository.getShortcut(d.packageName, d.id);
                match = new PartialDescriptorMatch(d, shortcut, PartialMatch.Type.EXACT);
            } else if (descriptor instanceof PinnedShortcutDescriptor) {
                match = new PartialDescriptorMatch((PinnedShortcutDescriptor) descriptor, PartialMatch.Type.EXACT);
            } else {
                continue;
            }
            matches.add(match);
            if (++count >= MAX_RESULTS_RECENT) {
                break;
            }
        }
        return matches;
    }

    private static final class MatchComparator implements Comparator<Match> {
        @Override
        public int compare(Match lhs, Match rhs) {
            int compareKind = lhs.getKind().compareTo(rhs.getKind());
            if (compareKind != 0) {
                return compareKind;
            }
            if (lhs instanceof PartialMatch && rhs instanceof PartialMatch) {
                return ((PartialMatch) lhs).compareTo(((PartialMatch) rhs));
            }
            return 0;
        }
    }
}

