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
import com.italankin.lnch.util.ShortcutUtils;
import com.italankin.lnch.util.picasso.ShortcutRequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.N_MR1)
public class AppShortcutsRepository implements ShortcutsRepository {
    private static final int ALL = ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_DYNAMIC;
    private static final int PINNED = ShortcutQuery.FLAG_MATCH_PINNED;

    private final LauncherApps launcherApps;
    private final DescriptorProvider descriptorProvider;

    private final ConcurrentHashMap<String, List<Shortcut>> shortcuts = new ConcurrentHashMap<>();

    public AppShortcutsRepository(Context context, DescriptorProvider descriptorProvider) {
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.descriptorProvider = descriptorProvider;
    }

    @Override
    public Completable loadShortcuts() {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Completable.complete();
        }
        return Observable.defer(() -> Observable.fromIterable(descriptorProvider.getDescriptors()))
                .ofType(AppDescriptor.class)
                .collectInto(new HashMap<String, List<Shortcut>>(), (map, descriptor) -> {
                    map.put(descriptor.getId(), queryShortcuts(descriptor));
                })
                .doOnSuccess(result -> {
                    shortcuts.clear();
                    shortcuts.putAll(result);
                })
                .ignoreElement();
    }

    @Override
    public List<Shortcut> getShortcuts(AppDescriptor descriptor) {
        List<Shortcut> list = shortcuts.get(descriptor.getId());
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public Completable loadShortcuts(AppDescriptor descriptor) {
        return Single.fromCallable(() -> queryShortcuts(descriptor))
                .doOnSuccess(list -> shortcuts.put(descriptor.getId(), list))
                .ignoreElement();
    }

    @Override
    public Shortcut getShortcut(String packageName, String shortcutId) {
        List<ShortcutInfo> list = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        return list.isEmpty() ? null : new AppShortcut(launcherApps, list.get(0));
    }

    @Override
    public Completable pinShortcut(Shortcut shortcut) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Completable.error(new RuntimeException("No permission"));
        }
        return Completable.fromRunnable(() -> {
            String packageName = shortcut.getPackageName();
            List<String> pinned = getPinnedShortcutIds(packageName);
            pinned.add(shortcut.getId());
            launcherApps.pinShortcuts(packageName, pinned, Process.myUserHandle());
        });
    }

    @Override
    public void unpinShortcut(String packageName, String shortcutId) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return;
        }
        List<String> pinned = getPinnedShortcutIds(packageName);
        pinned.remove(shortcutId);
        launcherApps.pinShortcuts(packageName, pinned, Process.myUserHandle());
    }

    @Override
    public List<Shortcut> getPinnedShortcuts() {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        ShortcutQuery query = new ShortcutQuery().setQueryFlags(PINNED);
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        if (shortcuts == null || shortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Shortcut> result = new ArrayList<>();
        for (ShortcutInfo shortcut : shortcuts) {
            result.add(new AppShortcut(launcherApps, shortcut));
        }
        Collections.sort(result);
        return result;
    }

    private List<String> getPinnedShortcutIds(String packageName) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        ShortcutQuery query = new ShortcutQuery()
                .setQueryFlags(PINNED)
                .setPackage(packageName);
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        List<String> pinned = new ArrayList<>();
        if (shortcuts != null && !shortcuts.isEmpty()) {
            for (ShortcutInfo shortcutInfo : shortcuts) {
                pinned.add(shortcutInfo.getId());
            }
        }
        return pinned;
    }

    private List<Shortcut> queryShortcuts(AppDescriptor descriptor) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        ShortcutQuery query = new ShortcutQuery();
        query.setQueryFlags(ALL);
        if (descriptor.componentName != null) {
            query.setActivity(ComponentName.unflattenFromString(descriptor.componentName));
        } else {
            query.setPackage(descriptor.packageName);
        }
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        if (shortcuts == null) {
            return Collections.emptyList();
        }
        List<Shortcut> result = new ArrayList<>(shortcuts.size());
        for (ShortcutInfo info : shortcuts) {
            if (!info.isEnabled()) {
                continue;
            }
            result.add(new AppShortcut(launcherApps, info));
        }
        Collections.sort(result);
        return result;
    }

    private static class AppShortcut implements Shortcut {
        private final LauncherApps launcherApps;
        private final ShortcutInfo shortcutInfo;

        public AppShortcut(LauncherApps launcherApps, ShortcutInfo info) {
            this.launcherApps = launcherApps;
            this.shortcutInfo = info;
        }

        @Override
        public boolean start(Rect bounds, Bundle options) {
            try {
                //noinspection ConstantConditions
                launcherApps.startShortcut(shortcutInfo, bounds, options);
                return true;
            } catch (Exception e) {
                Timber.w(e, "start");
                return false;
            }
        }

        @Override
        public CharSequence getShortLabel() {
            CharSequence label = shortcutInfo.getShortLabel();
            return label != null ? label : getLongLabel();
        }

        @Override
        public CharSequence getLongLabel() {
            CharSequence longLabel = shortcutInfo.getLongLabel();
            return longLabel != null ? longLabel : "";
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
        public int getRank() {
            return shortcutInfo.getRank();
        }

        @Override
        public int compareTo(@NonNull Shortcut that) {
            if (this.isDynamic() == that.isDynamic()) {
                return Integer.compare(this.getRank(), that.getRank());
            }
            return this.isDynamic() ? 1 : -1;
        }
    }
}
