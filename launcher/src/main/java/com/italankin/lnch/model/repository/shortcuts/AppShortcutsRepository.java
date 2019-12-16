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
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.actions.AddAction;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.ShortcutUtils;
import com.italankin.lnch.util.picasso.ShortcutIconHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.N_MR1)
public class AppShortcutsRepository implements ShortcutsRepository {

    private final LauncherApps launcherApps;
    private final Lazy<DescriptorRepository> descriptorRepository;
    private final NameNormalizer nameNormalizer;

    private final Map<Descriptor, List<Shortcut>> shortcutsCache = new ConcurrentHashMap<>();

    public AppShortcutsRepository(Context context, Lazy<DescriptorRepository> descriptorRepository,
            NameNormalizer nameNormalizer) {
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.descriptorRepository = descriptorRepository;
        this.nameNormalizer = nameNormalizer;
        launcherApps.registerCallback(new Callback());
    }

    @Override
    public Completable loadShortcuts() {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Completable.complete();
        }
        return Observable
                .defer(() -> Observable.fromIterable(descriptorRepository.get().itemsOfType(AppDescriptor.class)))
                .collectInto(new HashMap<Descriptor, List<Shortcut>>(),
                        (map, descriptor) -> map.put(descriptor, queryShortcuts(descriptor)))
                .doOnSuccess(result -> {
                    shortcutsCache.clear();
                    shortcutsCache.putAll(result);
                })
                .ignoreElement();
    }

    @Override
    public List<Shortcut> getShortcuts(AppDescriptor descriptor) {
        List<Shortcut> list = shortcutsCache.get(descriptor);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public Completable loadShortcuts(AppDescriptor descriptor) {
        return Single.fromCallable(() -> queryShortcuts(descriptor))
                .doOnSuccess(list -> shortcutsCache.put(descriptor, list))
                .ignoreElement();
    }

    @Override
    public Shortcut getShortcut(String packageName, String shortcutId) {
        List<ShortcutInfo> list = ShortcutUtils.findById(launcherApps, packageName, shortcutId);
        return list.isEmpty() ? null : new AppShortcut(launcherApps, list.get(0));
    }

    @Override
    public Completable pinShortcut(Shortcut shortcut) {
        return Completable
                .defer(() -> {
                    String packageName = shortcut.getPackageName();
                    AppDescriptor app = findAppByPackageName(packageName);
                    DeepShortcutDescriptor descriptor = DescriptorUtils.makeDeepShortcut(
                            shortcut, app, nameNormalizer);
                    return descriptorRepository.get().edit()
                            .enqueue(new AddAction(descriptor))
                            .commit();
                });
    }

    private List<Shortcut> queryShortcuts(AppDescriptor descriptor) {
        if (!launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        ShortcutQuery query = new ShortcutQuery();
        query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_DYNAMIC);
        if (descriptor.componentName != null) {
            query.setActivity(ComponentName.unflattenFromString(descriptor.componentName));
        } else {
            query.setPackage(descriptor.packageName);
        }
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
        return makeShortcuts(shortcuts);
    }

    private List<Shortcut> makeShortcuts(List<ShortcutInfo> shortcuts) {
        if (shortcuts == null || shortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Shortcut> result = new ArrayList<>(shortcuts.size());
        for (ShortcutInfo info : shortcuts) {
            result.add(new AppShortcut(launcherApps, info));
        }
        Collections.sort(result);
        return result;
    }

    private AppDescriptor findAppByPackageName(String packageName) {
        for (AppDescriptor item : descriptorRepository.get().itemsOfType(AppDescriptor.class)) {
            if (packageName.equals(item.packageName)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Cannot find app with packageName=" + packageName);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Shortcut
    ///////////////////////////////////////////////////////////////////////////

    private static class AppShortcut implements Shortcut {
        private final LauncherApps launcherApps;
        private final ShortcutInfo shortcutInfo;

        AppShortcut(LauncherApps launcherApps, ShortcutInfo info) {
            this.launcherApps = launcherApps;
            this.shortcutInfo = info;
        }

        @Override
        public boolean start(Rect bounds, Bundle options) {
            Timber.d("start: %s", shortcutInfo);
            if (!launcherApps.hasShortcutHostPermission()) {
                Timber.e("no permission to start shortcut");
                return false;
            }
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
            return ShortcutIconHandler.uriFrom(this);
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
        public boolean isEnabled() {
            return shortcutInfo.isEnabled();
        }

        @Override
        public CharSequence getDisabledMessage() {
            return shortcutInfo.getDisabledMessage();
        }

        @Override
        public int getRank() {
            return shortcutInfo.getRank();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // LauncherApps.Callback
    ///////////////////////////////////////////////////////////////////////////

    private class Callback extends LauncherApps.Callback {
        @Override
        public void onShortcutsChanged(@NonNull String packageName,
                @NonNull List<ShortcutInfo> shortcuts, @NonNull UserHandle user) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            Set<ComponentName> componentNames = new HashSet<>(shortcuts.size());
            for (ShortcutInfo shortcut : shortcuts) {
                componentNames.add(shortcut.getActivity());
            }

            List<AppDescriptor> updated = new ArrayList<>(1);
            for (AppDescriptor descriptor : findByPackageName(packageName)) {
                if (descriptor.componentName == null || componentNames.contains(
                        ComponentName.unflattenFromString(descriptor.componentName))) {
                    updated.add(descriptor);
                }
            }

            updateCache(updated);
        }

        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            for (AppDescriptor descriptor : findByPackageName(packageName)) {
                shortcutsCache.remove(descriptor);
            }
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            updateCache(findByPackageName(packageName));
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            updateCache(findByPackageName(packageName));
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            for (String packageName : packageNames) {
                updateCache(findByPackageName(packageName));
            }
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            if (!Process.myUserHandle().equals(user)) {
                return;
            }
            for (String packageName : packageNames) {
                List<AppDescriptor> descriptors = findByPackageName(packageName);
                for (AppDescriptor descriptor : descriptors) {
                    shortcutsCache.remove(descriptor);
                }
            }
        }

        private List<AppDescriptor> findByPackageName(String packageName) {
            List<AppDescriptor> result = new ArrayList<>(1);
            for (AppDescriptor descriptor : descriptorRepository.get().itemsOfType(AppDescriptor.class)) {
                if (descriptor.packageName.equals(packageName)) {
                    result.add(descriptor);
                }
            }
            return result;
        }

        private void updateCache(List<AppDescriptor> descriptors) {
            for (AppDescriptor descriptor : descriptors) {
                shortcutsCache.put(descriptor, queryShortcuts(descriptor));
            }
        }
    }
}
