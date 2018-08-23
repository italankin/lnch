package com.italankin.lnch.model.repository.search;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.model.repository.search.match.UrlMatch;
import com.italankin.lnch.model.repository.search.match.WebSearchMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        String s = constraint.toString().trim().toLowerCase();
        if (s.isEmpty()) {
            return Collections.emptyList();
        }
        List<PartialMatch> matches = new ArrayList<>(8);
        for (Descriptor item : appsRepository.items()) {
            if (!(item instanceof AppDescriptor)) {
                continue;
            }
            AppDescriptor appItem = (AppDescriptor) item;
            PartialMatch match = null;
            if (startsWith(appItem.customLabel, s) || startsWith(appItem.label, s)) {
                match = new PartialMatch(PartialMatch.Type.STARTS_WITH);
            } else if (containsWord(appItem.customLabel, s) || containsWord(appItem.label, s)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS_WORD);
            } else if (contains(appItem.customLabel, s) || contains(appItem.label, s)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS);
            }
            if (match != null) {
                match.color = item.getVisibleColor();
                match.label = appItem.getVisibleLabel();
                match.intent = packageManager.getLaunchIntentForPackage(appItem.packageName);
                if (match.intent != null && appItem.componentName != null) {
                    match.intent.setComponent(ComponentName.unflattenFromString(appItem.componentName));
                }
                try {
                    match.icon = packageManager.getApplicationIcon(appItem.packageName);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                matches.add(match);
            }
        }

        if (matches.size() > 1) {
            Collections.sort(matches);
            matches = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        }

        matches.add(new WebSearchMatch(constraint.toString(), s));

        if (s.startsWith("http://") && s.length() > 7 || s.startsWith("https://") && s.length() > 8
                || s.matches("\\w+\\.\\w+")) {
            matches.add(new UrlMatch(s));
        }

        return matches;
    }
}

