package com.italankin.lnch.model.repository.search;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.search.match.UrlMatch;
import com.italankin.lnch.model.repository.search.match.WebSearchMatch;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.picasso.PackageManagerRequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static android.util.Patterns.WEB_URL;
import static com.italankin.lnch.model.repository.prefs.Preferences.SearchTarget;
import static com.italankin.lnch.model.repository.search.match.PartialMatch.Type;
import static com.italankin.lnch.util.SearchUtils.contains;
import static com.italankin.lnch.util.SearchUtils.containsWord;
import static com.italankin.lnch.util.SearchUtils.startsWith;

public class SearchRepositoryImpl implements SearchRepository {

    public static final int MAX_RESULTS = 4;

    private final AppsRepository appsRepository;
    private final PackageManager packageManager;
    private final ShortcutsRepository shortcutsRepository;
    private final Preferences preferences;

    public SearchRepositoryImpl(PackageManager packageManager, AppsRepository appsRepository,
            ShortcutsRepository shortcutsRepository, Preferences preferences) {
        this.appsRepository = appsRepository;
        this.packageManager = packageManager;
        this.shortcutsRepository = shortcutsRepository;
        this.preferences = preferences;
    }

    @Override
    public List<? extends Match> search(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return Collections.emptyList();
        }
        String query = constraint.toString().trim().toLowerCase();
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        EnumSet<SearchTarget> searchTargets = preferences.searchTargets();
        List<PartialMatch> matches = new ArrayList<>(8);
        for (Descriptor descriptor : appsRepository.items()) {
            PartialMatch match = null;
            if (descriptor instanceof AppDescriptor) {
                AppDescriptor appDescriptor = (AppDescriptor) descriptor;
                if (appDescriptor.hidden && !searchTargets.contains(SearchTarget.HIDDEN)) {
                    continue;
                }
                match = testApp(appDescriptor, query, packageManager);
            } else if (descriptor instanceof PinnedShortcutDescriptor
                    && searchTargets.contains(SearchTarget.SHORTCUT)) {
                match = testShortcut((PinnedShortcutDescriptor) descriptor, query);
            }
            if (match != null) {
                matches.add(match);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
                && searchTargets.contains(SearchTarget.SHORTCUT)) {
            List<PartialMatch> shortcutMatches = searchShortcuts(appsRepository.items(), query);
            matches.addAll(shortcutMatches);
        }

        if (matches.size() > 1) {
            Collections.sort(matches);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }

        if (searchTargets.contains(SearchTarget.WEB)) {
            matches.add(new WebSearchMatch(constraint.toString(), query));
        }

        if (searchTargets.contains(SearchTarget.URL)) {
            if (WEB_URL.matcher(query).matches() || WEB_URL.matcher("http://" + query).matches()) {
                matches.add(new UrlMatch(query));
            }
        }

        return matches;
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private List<PartialMatch> searchShortcuts(List<Descriptor> descriptors, String query) {
        List<PartialMatch> result = new ArrayList<>(1);
        for (Descriptor descriptor : descriptors) {
            if (descriptor instanceof AppDescriptor) {
                List<Shortcut> shortcuts = shortcutsRepository.getShortcuts((AppDescriptor) descriptor);
                if (shortcuts == null || shortcuts.isEmpty()) {
                    continue;
                }
                for (Shortcut shortcut : shortcuts) {
                    if (!shortcut.isDynamic() && contains(shortcut.getShortLabel().toString(), query)) {
                        PartialMatch match = new PartialMatch(Type.OTHER);
                        match.icon = PackageManagerRequestHandler.uriFrom(shortcut.getPackageName());
                        match.label = shortcut.getShortLabel();
                        match.color = Color.WHITE;
                        match.intent = StartShortcutReceiver.makeStartIntent(shortcut);
                        result.add(match);
                    }
                }
            }
        }
        return result;
    }

    private static PartialMatch testApp(AppDescriptor item, String query, PackageManager packageManager) {
        PartialMatch match = test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = packageManager.getLaunchIntentForPackage(item.packageName);
            if (match.intent != null && item.componentName != null) {
                match.intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            match.icon = PackageManagerRequestHandler.uriFrom(item.packageName);
        }
        return match;
    }

    private static PartialMatch testShortcut(PinnedShortcutDescriptor item, String query) {
        PartialMatch match = test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = IntentUtils.fromUri(item.uri);
            match.iconRes = R.drawable.ic_shortcut;
        }
        return match;
    }

    private static PartialMatch test(Descriptor descriptor, String query) {
        PartialMatch match = null;
        if (descriptor instanceof CustomLabelDescriptor) {
            CustomLabelDescriptor item = (CustomLabelDescriptor) descriptor;
            String label = item.getLabel();
            String customLabel = item.getCustomLabel();
            if (startsWith(customLabel, query) || startsWith(label, query)) {
                match = new PartialMatch(Type.STARTS_WITH);
            } else if (containsWord(customLabel, query) || containsWord(label, query)) {
                match = new PartialMatch(Type.CONTAINS_WORD);
            } else if (contains(customLabel, query) || contains(label, query)) {
                match = new PartialMatch(Type.CONTAINS);
            }
            if (match != null) {
                match.label = item.getVisibleLabel();
            }
        } else if (descriptor instanceof LabelDescriptor) {
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (startsWith(label, query)) {
                match = new PartialMatch(Type.STARTS_WITH);
            } else if (containsWord(label, query)) {
                match = new PartialMatch(Type.CONTAINS_WORD);
            } else if (contains(label, query)) {
                match = new PartialMatch(Type.CONTAINS);
            }
            if (match != null) {
                match.label = label;
            }
        }
        return match;
    }
}

