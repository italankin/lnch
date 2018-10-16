package com.italankin.lnch.model.repository.search;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;
import com.italankin.lnch.model.repository.descriptors.model.ShortcutDescriptor;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.search.match.UrlMatch;
import com.italankin.lnch.model.repository.search.match.WebSearchMatch;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.picasso.PackageManagerRequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.util.Patterns.WEB_URL;
import static com.italankin.lnch.util.SearchUtils.contains;
import static com.italankin.lnch.util.SearchUtils.containsWord;
import static com.italankin.lnch.util.SearchUtils.startsWith;

public class SearchRepositoryImpl implements SearchRepository {

    public static final int MAX_RESULTS = 4;
    private final AppsRepository appsRepository;
    private final PackageManager packageManager;

    public SearchRepositoryImpl(PackageManager packageManager, AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
        this.packageManager = packageManager;
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
        List<PartialMatch> matches = new ArrayList<>(8);
        for (Descriptor item : appsRepository.items()) {
            PartialMatch match = null;
            if (item instanceof AppDescriptor) {
                match = testApp((AppDescriptor) item, query);
            } else if (item instanceof ShortcutDescriptor) {
                match = testShortcut((ShortcutDescriptor) item, query);
            }
            if (match != null) {
                matches.add(match);
            }
        }

        if (matches.size() > 1) {
            Collections.sort(matches);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }

        matches.add(new WebSearchMatch(constraint.toString(), query));

        if (WEB_URL.matcher(query).matches() || WEB_URL.matcher("http://" + query).matches()) {
            matches.add(new UrlMatch(query));
        }

        return matches;
    }

    public PartialMatch testApp(AppDescriptor item, String query) {
        PartialMatch match = null;
        if (startsWith(item.customLabel, query) || startsWith(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.STARTS_WITH);
        } else if (containsWord(item.customLabel, query) || containsWord(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.CONTAINS_WORD);
        } else if (contains(item.customLabel, query) || contains(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.CONTAINS);
        }
        if (match != null) {
            match.color = item.getVisibleColor();
            match.label = item.getVisibleLabel();
            match.intent = packageManager.getLaunchIntentForPackage(item.packageName);
            if (match.intent != null && item.componentName != null) {
                match.intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            match.icon = PackageManagerRequestHandler.uriFrom(item.packageName);
        }
        return match;
    }

    private PartialMatch testShortcut(ShortcutDescriptor item, String query) {
        PartialMatch match = null;
        if (startsWith(item.customLabel, query) || startsWith(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.STARTS_WITH);
        } else if (containsWord(item.customLabel, query) || containsWord(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.CONTAINS_WORD);
        } else if (contains(item.customLabel, query) || contains(item.label, query)) {
            match = new PartialMatch(PartialMatch.Type.CONTAINS);
        }
        if (match != null) {
            match.color = item.getVisibleColor();
            match.label = item.getVisibleLabel();
            match.intent = IntentUtils.fromUri(item.uri);
            match.iconRes = R.drawable.ic_shortcut;
        }
        return match;
    }
}

