package com.italankin.lnch.model.repository.search.delegate;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.picasso.PackageIconHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AppSearchDelegate implements SearchDelegate {

    private final PackageManager packageManager;
    private final AppsRepository appsRepository;

    public AppSearchDelegate(PackageManager packageManager, AppsRepository appsRepository) {
        this.packageManager = packageManager;
        this.appsRepository = appsRepository;
    }

    @Override
    public List<PartialMatch> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        boolean skipHidden = !searchTargets.contains(Preferences.SearchTarget.HIDDEN);
        List<PartialMatch> matches = new ArrayList<>(4);
        for (Descriptor descriptor : appsRepository.items()) {
            if (descriptor instanceof AppDescriptor) {
                AppDescriptor appDescriptor = (AppDescriptor) descriptor;
                if (appDescriptor.hidden && skipHidden) {
                    continue;
                }
                PartialMatch match = testApp(appDescriptor, query);
                if (match != null) {
                    matches.add(match);
                }
            }
        }
        return matches;
    }

    private PartialMatch testApp(AppDescriptor item, String query) {
        PartialMatch match = DescriptorSearchUtils.test(item, query);
        if (match != null) {
            match.color = item.getVisibleColor();
            match.intent = packageManager.getLaunchIntentForPackage(item.packageName);
            if (match.intent != null && item.componentName != null) {
                match.intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            match.icon = PackageIconHandler.uriFrom(item.packageName);
            match.descriptor = item;
        }
        return match;
    }
}
