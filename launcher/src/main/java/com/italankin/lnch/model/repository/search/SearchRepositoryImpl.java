package com.italankin.lnch.model.repository.search;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.WorkerThread;

import com.italankin.lnch.model.AppItem;
import com.italankin.lnch.model.repository.apps.IAppsRepository;
import com.italankin.lnch.model.repository.search.match.GoogleMatch;
import com.italankin.lnch.model.repository.search.match.IMatch;
import com.italankin.lnch.model.repository.search.match.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchRepositoryImpl implements ISearchRepository {

    private final IAppsRepository appsRepository;
    private final PackageManager packageManager;

    public SearchRepositoryImpl(Context context, IAppsRepository appsRepository) {
        this.appsRepository = appsRepository;
        this.packageManager = context.getPackageManager();
    }

    @WorkerThread
    @Override
    public List<? extends IMatch> search(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return Collections.emptyList();
        }
        String s = constraint.toString().toLowerCase();
        List<Match> matches = new ArrayList<>(8);
        for (AppItem appItem : appsRepository.getApps()) {
            Match match = null;
            if (startsWith(appItem.customLabel, s)) {
                match = new Match(Match.Type.STARTS_WITH);
            } else if (startsWith(appItem.label, s)) {
                match = new Match(Match.Type.STARTS_WITH);
            } else if (contains(appItem.customLabel, s)) {
                match = new Match(Match.Type.CONTAINS);
            } else if (contains(appItem.label, s)) {
                match = new Match(Match.Type.CONTAINS);
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

        matches.add(new GoogleMatch(s));

        Collections.sort(matches);

        return matches;
    }

    private static boolean contains(String what, String substring) {
        return what != null && substring != null && what.toLowerCase(Locale.getDefault()).contains(substring);
    }

    private static boolean startsWith(String what, String prefix) {
        return what != null && prefix != null && what.toLowerCase(Locale.getDefault()).startsWith(prefix);
    }
}

