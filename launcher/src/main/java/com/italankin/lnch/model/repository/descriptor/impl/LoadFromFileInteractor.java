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
                    ProcessingEnv env = new ProcessingEnv(savedItems.size(), infoList,
                            shortcutsRepository.getPinnedShortcuts());
                    for (Descriptor item : savedItems) {
                        if (item instanceof PinnedShortcutDescriptor) {
                            visitPinnedShortcut(env, (PinnedShortcutDescriptor) item);
                        } else if (item instanceof DeepShortcutDescriptor) {
                            visitDeepShortcut(env, (DeepShortcutDescriptor) item);
                        } else if (item instanceof AppDescriptor) {
                            visitAppDescriptor(env, (AppDescriptor) item);
                        } else {
                            env.add(item);
                        }
                    }
                    for (List<LauncherActivityInfo> infos : env.infosByPackageName.values()) {
                        processNewApps(env, infos);
                    }
                    List<DeepShortcutDescriptor> shortcuts = processNewShortcuts(env);
                    env.add(shortcuts);
                    emitter.onSuccess(new AppsData(env.items, env.isChanged()));
                });
    }

    private void visitAppDescriptor(ProcessingEnv env, AppDescriptor item) {
        LauncherActivityInfo info = env.findInfo(item);
        if (info != null) {
            appDescriptors.update(item, info);
            env.add(item);
        } else {
            env.delete(item);
        }
    }

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor descriptor) {
        env.add(descriptor);
        descriptor.enabled = false;
        Shortcut shortcut = env.findShortcut(descriptor.packageName, descriptor.id);
        if (shortcut != null) {
            descriptor.enabled = shortcut.isEnabled();
        }
    }

    private void visitPinnedShortcut(ProcessingEnv processingEnv, PinnedShortcutDescriptor item) {
        Intent intent = IntentUtils.fromUri(item.uri);
        if (IntentUtils.canHandleIntent(packageManager, intent)) {
            processingEnv.add(item);
        } else {
            processingEnv.delete(item);
        }
    }

    private void processNewApps(ProcessingEnv env, List<LauncherActivityInfo> infos) {
        if (infos.size() == 1) {
            AppDescriptor item = appDescriptors.create(infos.get(0));
            env.add(item);
        } else {
            for (LauncherActivityInfo info : infos) {
                AppDescriptor item = appDescriptors.create(info, true);
                env.add(item);
            }
        }
    }

    private List<DeepShortcutDescriptor> processNewShortcuts(ProcessingEnv env) {
        if (env.pinnedShortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeepShortcutDescriptor> result = new ArrayList<>(env.pinnedShortcuts.size());
        for (Shortcut shortcut : env.pinnedShortcuts) {
            AppDescriptor app = env.findApp(shortcut.getPackageName());
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

    private static class ProcessingEnv {
        private final List<Descriptor> items;
        private final Map<String, List<LauncherActivityInfo>> infosByPackageName;
        private final Map<String, AppDescriptor> installedApps;
        private final List<Shortcut> pinnedShortcuts;
        private boolean hasDeletions = false;

        ProcessingEnv(int expectedSize, List<LauncherActivityInfo> infoList, List<Shortcut> pinnedShortcuts) {
            this.items = new ArrayList<>(expectedSize);
            this.infosByPackageName = mapInfosByPackageName(infoList);
            this.installedApps = new HashMap<>(expectedSize);
            this.pinnedShortcuts = pinnedShortcuts;
        }

        void add(Descriptor item) {
            items.add(item);
        }

        void add(List<? extends Descriptor> items) {
            this.items.addAll(items);
        }

        void add(AppDescriptor item) {
            items.add(item);
            installedApps.put(item.packageName, item);
        }

        void delete(Descriptor item) {
            hasDeletions = true;
        }

        AppsData getResult() {
            return new AppsData(items, isChanged());
        }

        AppDescriptor findApp(String packageName) {
            return installedApps.get(packageName);
        }

        LauncherActivityInfo findInfo(AppDescriptor item) {
            List<LauncherActivityInfo> infos = infosByPackageName.get(item.packageName);
            if (infos == null || infos.isEmpty()) {
                return null;
            }
            if (infos.size() == 1) {
                LauncherActivityInfo result = infos.remove(0);
                infosByPackageName.remove(item.packageName);
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

        Shortcut findShortcut(String packageName, String id) {
            for (Iterator<Shortcut> iter = pinnedShortcuts.iterator(); iter.hasNext(); ) {
                Shortcut shotcut = iter.next();
                if (shotcut.getPackageName().equals(packageName)
                        && shotcut.getId().equals(id)) {
                    iter.remove();
                    return shotcut;
                }
            }
            return null;
        }

        boolean isChanged() {
            return hasDeletions || !infosByPackageName.isEmpty() || !pinnedShortcuts.isEmpty();
        }

        private Map<String, List<LauncherActivityInfo>> mapInfosByPackageName(List<LauncherActivityInfo> infoList) {
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
    }
}
