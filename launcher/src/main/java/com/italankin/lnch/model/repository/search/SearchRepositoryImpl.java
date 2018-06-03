package com.italankin.lnch.model.repository.search;

import android.content.pm.PackageManager;
import android.support.annotation.WorkerThread;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.search.match.GoogleMatch;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.search.match.MatchImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchRepositoryImpl implements SearchRepository {

    public static final int MAX_RESULTS = 4;
    private final AppsRepository appsRepository;
    private final PackageManager packageManager;

    public SearchRepositoryImpl(PackageManager packageManager, AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
        this.packageManager = packageManager;
    }

    @WorkerThread
    @Override
    public List<? extends Match> search(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return Collections.emptyList();
        }
        String s = constraint.toString().trim().toLowerCase();
        if (s.isEmpty()) {
            return Collections.emptyList();
        }
        List<MatchImpl> matches = new ArrayList<>(8);
        for (AppItem appItem : appsRepository.getApps()) {
            MatchImpl match = null;
            if (startsWith(appItem.customLabel, s) || startsWith(appItem.label, s)) {
                match = new MatchImpl(MatchImpl.Type.STARTS_WITH);
            } else if (containsWord(appItem.customLabel, s) || containsWord(appItem.label, s)) {
                match = new MatchImpl(MatchImpl.Type.CONTAINS_WORD);
            } else if (contains(appItem.customLabel, s) || contains(appItem.label, s)) {
                match = new MatchImpl(MatchImpl.Type.CONTAINS);
            }
            if (match != null) {
                match.color = appItem.getColor();
                match.label = appItem.getLabel();
                match.intent = packageManager.getLaunchIntentForPackage(appItem.packageName);
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

        matches.add(new GoogleMatch(s));

        return matches;
    }

    private static boolean contains(String what, String substring) {
        return what != null && substring != null && what.toLowerCase(Locale.getDefault()).contains(substring);
    }

    private static boolean containsWord(String what, String word) {
        if (what == null || word == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\b" + word.toLowerCase());
        Matcher matcher = pattern.matcher(what.toLowerCase());
        return matcher.find();
    }

    private static boolean startsWith(String what, String prefix) {
        return what != null && prefix != null && what.toLowerCase(Locale.getDefault()).startsWith(prefix);
    }
}

