package com.italankin.lnch.model.repository.descriptor.impl;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.IntentUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Maybe;

import static com.italankin.lnch.model.repository.descriptor.impl.LauncherActivityInfoUtils.getComponentName;

class LoadFromFileInteractor {
    private final PackagesStore packagesStore;
    private final DescriptorStore descriptorStore;
    private final ShortcutsRepository shortcutsRepository;
    private final PackageManager packageManager;
    private final AppDescriptors appDescriptors;

    LoadFromFileInteractor(PackagesStore packagesStore, DescriptorStore descriptorStore,
            ShortcutsRepository shortcutsRepository, PackageManager packageManager,
            AppDescriptors appDescriptors) {
        this.packagesStore = packagesStore;
        this.descriptorStore = descriptorStore;
        this.shortcutsRepository = shortcutsRepository;
        this.packageManager = packageManager;
        this.appDescriptors = appDescriptors;
    }

    Maybe<AppsData> loadFromFile(List<LauncherActivityInfo> infoList) {
        return Maybe
                .create(emitter -> {
                    InputStream packagesInput = packagesStore.input();
                    if (packagesInput == null) {
                        emitter.onComplete();
                        return;
                    }
                    List<Descriptor> savedItems = descriptorStore.read(packagesInput);
                    if (savedItems == null) {
                        emitter.onComplete();
                        return;
                    }
                    List<Descriptor> items = new ArrayList<>(savedItems.size());
                    List<Descriptor> deleted = new ArrayList<>(8);
                    Map<String, List<LauncherActivityInfo>> infosByPackageName = infosByPackageName(infoList);
                    List<Shortcut> pinnedShortcuts = shortcutsRepository.getPinnedShortcuts();
                    Map<String, AppDescriptor> installedApps = new HashMap<>(savedItems.size());
                    for (Descriptor item : savedItems) {
                        if (item instanceof PinnedShortcutDescriptor) {
                            String uri = ((PinnedShortcutDescriptor) item).uri;
                            Intent intent = IntentUtils.fromUri(uri);
                            if (IntentUtils.canHandleIntent(packageManager, intent)) {
                                items.add(item);
                            } else {
                                deleted.add(item);
                            }
                            continue;
                        }
                        if (item instanceof DeepShortcutDescriptor) {
                            items.add(item);
                            DeepShortcutDescriptor descriptor = (DeepShortcutDescriptor) item;
                            if (pinnedShortcuts.isEmpty()) {
                                descriptor.enabled = false;
                                continue;
                            }
                            boolean enabled = false;
                            for (Iterator<Shortcut> iter = pinnedShortcuts.iterator(); iter.hasNext(); ) {
                                Shortcut pinned = iter.next();
                                if (pinned.getPackageName().equals(descriptor.packageName)
                                        && pinned.getId().equals(descriptor.id)) {
                                    iter.remove();
                                    enabled = pinned.isEnabled();
                                    break;
                                }
                            }
                            descriptor.enabled = enabled;
                            continue;
                        }
                        if (!(item instanceof AppDescriptor)) {
                            items.add(item);
                            continue;
                        }
                        AppDescriptor app = (AppDescriptor) item;
                        LauncherActivityInfo info = findInfo(infosByPackageName, app);
                        if (info != null) {
                            appDescriptors.update(app, info);
                            items.add(app);
                            installedApps.put(app.packageName, app);
                        } else {
                            deleted.add(app);
                        }
                    }
                    for (List<LauncherActivityInfo> infos : infosByPackageName.values()) {
                        if (infos.size() == 1) {
                            AppDescriptor item = appDescriptors.create(infos.get(0));
                            items.add(item);
                            installedApps.put(item.packageName, item);
                        } else {
                            for (LauncherActivityInfo info : infos) {
                                AppDescriptor item = appDescriptors.create(info, true);
                                items.add(item);
                                installedApps.put(item.packageName, item);
                            }
                        }
                    }
                    List<DeepShortcutDescriptor> shortcuts = processShortcuts(pinnedShortcuts, installedApps);
                    if (!shortcuts.isEmpty()) {
                        items.addAll(shortcuts);
                    }
                    boolean changed = !deleted.isEmpty() || !infosByPackageName.isEmpty()
                            || !pinnedShortcuts.isEmpty();
                    emitter.onSuccess(new AppsData(items, changed));
                });
    }

    private List<DeepShortcutDescriptor> processShortcuts(List<Shortcut> pinnedShortcuts,
            Map<String, AppDescriptor> installedApps) {
        if (pinnedShortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeepShortcutDescriptor> result = new ArrayList<>(pinnedShortcuts.size());
        for (Shortcut shortcut : pinnedShortcuts) {
            AppDescriptor app = installedApps.get(shortcut.getPackageName());
            if (app == null) {
                continue;
            }
            DeepShortcutDescriptor item = new DeepShortcutDescriptor(app.packageName, shortcut.getId());
            item.color = app.color;
            String label = shortcut.getShortLabel().toString();
            if (TextUtils.isEmpty(label)) {
                item.label = app.getVisibleLabel();
            } else {
                item.label = label.toUpperCase(Locale.getDefault());
            }
            result.add(item);
        }
        return result;
    }

    private Map<String, List<LauncherActivityInfo>> infosByPackageName(List<LauncherActivityInfo> infoList) {
        Map<String, List<LauncherActivityInfo>> infosByPackageName = new HashMap<>(infoList.size());
        for (LauncherActivityInfo info : infoList) {
            String packageName = info.getApplicationInfo().packageName;
            List<LauncherActivityInfo> list = infosByPackageName.get(packageName);
            if (list == null) {
                list = new ArrayList<>(1);
                infosByPackageName.put(packageName, list);
            }
            list.add(info);
        }
        return infosByPackageName;
    }

    private LauncherActivityInfo findInfo(Map<String, List<LauncherActivityInfo>> map, AppDescriptor item) {
        List<LauncherActivityInfo> infos = map.get(item.packageName);
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        if (infos.size() == 1) {
            LauncherActivityInfo result = infos.remove(0);
            map.remove(item.packageName);
            return result;
        } else if (item.componentName != null) {
            Iterator<LauncherActivityInfo> iter = infos.iterator();
            while (iter.hasNext()) {
                LauncherActivityInfo info = iter.next();
                String componentName = getComponentName(info);
                if (componentName.equals(item.componentName)) {
                    iter.remove();
                    return info;
                }
            }
        }
        return null;
    }
}
