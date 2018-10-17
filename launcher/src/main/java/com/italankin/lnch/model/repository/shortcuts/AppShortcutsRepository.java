package com.italankin.lnch.model.repository.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.util.picasso.ShortcutRequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.N_MR1)
public class AppShortcutsRepository implements ShortcutsRepository {
    private final LauncherApps launcherApps;
    private final AppsRepository appsRepository;

    private volatile Map<String, List<Shortcut>> shortcuts = Collections.emptyMap();

    public AppShortcutsRepository(Context context, AppsRepository appsRepository) {
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.appsRepository = appsRepository;
    }

    @Override
    public Completable loadShortcuts() {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Completable.complete();
        }
        return Observable.defer(() -> Observable.fromIterable(appsRepository.items()))
                .ofType(AppDescriptor.class)
                .collectInto(new HashMap<String, List<Shortcut>>(), (map, descriptor) -> {
                    ShortcutQuery query = new ShortcutQuery();
                    query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST
                            | ShortcutQuery.FLAG_MATCH_DYNAMIC);
                    if (descriptor.componentName != null) {
                        query.setActivity(ComponentName.unflattenFromString(descriptor.componentName));
                    } else {
                        query.setPackage(descriptor.packageName);
                    }
                    List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
                    if (shortcuts == null) {
                        map.put(descriptor.getId(), Collections.emptyList());
                        return;
                    }
                    List<AppShortcut> result = new ArrayList<>(shortcuts.size());
                    for (ShortcutInfo info : shortcuts) {
                        if (!info.isEnabled()) {
                            continue;
                        }
                        result.add(new AppShortcut(info));
                    }
                    Collections.sort(result);
                    map.put(descriptor.getId(), new ArrayList<>(result));
                })
                .doOnSuccess(result -> shortcuts = result)
                .ignoreElement();
    }

    @Override
    public List<Shortcut> getShortcuts(AppDescriptor descriptor) {
        List<Shortcut> list = shortcuts.get(descriptor.getId());
        return list != null ? list : Collections.emptyList();
    }

    class AppShortcut implements Shortcut, Comparable<AppShortcut> {
        private final ShortcutInfo shortcutInfo;

        public AppShortcut(ShortcutInfo info) {
            shortcutInfo = info;
        }

        @Override
        public boolean start(Rect bounds, Bundle options) {
            try {
                launcherApps.startShortcut(shortcutInfo, bounds, options);
                return true;
            } catch (Exception e) {
                Timber.w(e, "start");
                return false;
            }
        }

        @Override
        public CharSequence getShortLabel() {
            return shortcutInfo.getShortLabel();
        }

        @Override
        public CharSequence getLongLabel() {
            return shortcutInfo.getLongLabel();
        }

        @Override
        public Uri getIconUri() {
            return ShortcutRequestHandler.uriFrom(this);
        }

        @Override
        public String getPackageName() {
            return shortcutInfo.getPackage();
        }

        @Override
        public String getId() {
            return shortcutInfo.getId();
        }

        @Override
        public boolean isDynamic() {
            return shortcutInfo.isDynamic();
        }

        @Override
        public int compareTo(@NonNull AppShortcut that) {
            if (this.shortcutInfo.isDynamic() == that.shortcutInfo.isDynamic()) {
                return Integer.compare(this.shortcutInfo.getRank(), that.shortcutInfo.getRank());
            }
            return this.shortcutInfo.isDynamic() ? 1 : -1;
        }
    }
}
