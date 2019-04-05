package com.italankin.lnch.model.repository.descriptor.apps;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Maybe;

import static com.italankin.lnch.model.repository.descriptor.apps.LauncherActivityInfoUtils.getComponentName;

class LoadFromFile {

    private final AppDescriptors appDescriptors;
    private final PackagesStore packagesStore;
    private final DescriptorStore descriptorStore;
    private final ShortcutsRepository shortcutsRepository;
    private final PackageManager packageManager;

    LoadFromFile(AppDescriptors appDescriptors, PackagesStore packagesStore,
            DescriptorStore descriptorStore, ShortcutsRepository shortcutsRepository,
            PackageManager packageManager) {
        this.appDescriptors = appDescriptors;
        this.packagesStore = packagesStore;
        this.descriptorStore = descriptorStore;
        this.shortcutsRepository = shortcutsRepository;
        this.packageManager = packageManager;
    }

    Maybe<AppsData> load(List<LauncherActivityInfo> infoList) {
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
                    ProcessingEnv env = new ProcessingEnv(shortcutsRepository.getPinnedShortcuts(), infoList);
                    for (Descriptor item : savedItems) {
                        if (item instanceof PinnedShortcutDescriptor) {
                            visitPinnedShortcut(env, (PinnedShortcutDescriptor) item);
                        } else if (item instanceof DeepShortcutDescriptor) {
                            visitDeepShortcut(env, (DeepShortcutDescriptor) item);
                        } else if (item instanceof AppDescriptor) {
                            visitApp(env, (AppDescriptor) item);
                        } else {
                            env.items.add(item);
                        }
                    }
                    processNewApps(env);
                    processShortcuts(env);
                    emitter.onSuccess(env.getData());
                });
    }


    private void processShortcuts(ProcessingEnv env) {
        for (Shortcut shortcut : env.shortcuts) {
            String packageName = shortcut.getPackageName();
            DeepShortcutDescriptor item = new DeepShortcutDescriptor(
                    packageName, shortcut.getId());
            AppDescriptor app = env.installed.get(packageName);
            assert app != null;
            item.color = app.color;
            String label = shortcut.getShortLabel().toString();
            if (TextUtils.isEmpty(label)) {
                item.label = app.getVisibleLabel();
            } else {
                item.label = label.toUpperCase(Locale.getDefault());
            }
            env.items.add(item);
        }
    }

    private void processNewApps(ProcessingEnv env) {
        for (List<LauncherActivityInfo> infos : env.packagesMap.lists()) {
            if (infos.size() == 1) {
                AppDescriptor item = appDescriptors.createItem(infos.get(0));
                env.items.add(item);
                env.installed.put(item.packageName, item);
            } else {
                for (LauncherActivityInfo info : infos) {
                    AppDescriptor item = appDescriptors.createItem(info, getComponentName(info));
                    env.items.add(item);
                    env.installed.put(item.packageName, item);
                }
            }
        }
    }

    private void visitApp(ProcessingEnv env, AppDescriptor app) {
        LauncherActivityInfo info = env.packagesMap.poll(app);
        if (info != null) {
            appDescriptors.updateItem(app, info);
            env.items.add(app);
            env.installed.put(app.packageName, app);
        } else {
            env.deleted.add(app);
        }
    }

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor item) {
        env.items.add(item);
        if (env.shortcuts.isEmpty()) {
            item.enabled = false;
            return;
        }
        for (Iterator<Shortcut> iter = env.shortcuts.iterator(); iter.hasNext(); ) {
            Shortcut pinned = iter.next();
            if (pinned.getPackageName().equals(item.packageName)
                    && pinned.getId().equals(item.id)) {
                iter.remove();
                item.enabled = pinned.isEnabled();
                return;
            }
        }
    }

    private void visitPinnedShortcut(ProcessingEnv env, PinnedShortcutDescriptor item) {
        String uri = item.uri;
        Intent intent = IntentUtils.fromUri(uri);
        if (IntentUtils.canHandleIntent(packageManager, intent)) {
            env.items.add(item);
        } else {
            env.deleted.add(item);
        }
    }

    private static class ProcessingEnv {
        final List<Descriptor> items = new ArrayList<>(64);
        final List<Descriptor> deleted = new ArrayList<>(8);
        final PackagesMap packagesMap;
        final List<Shortcut> shortcuts;
        final Map<String, AppDescriptor> installed = new HashMap<>(64);

        ProcessingEnv(List<Shortcut> shortcuts, List<LauncherActivityInfo> infoList) {
            this.shortcuts = shortcuts;
            this.packagesMap = new PackagesMap(infoList);
        }

        AppsData getData() {
            boolean changed = !deleted.isEmpty() || !packagesMap.isEmpty()
                    || !shortcuts.isEmpty();
            return new AppsData(items, changed);
        }
    }

    private static class PackagesMap {
        private final Map<String, List<LauncherActivityInfo>> packages;

        PackagesMap(List<LauncherActivityInfo> infoList) {
            this.packages = groupByPackage(infoList);
        }

        boolean isEmpty() {
            return packages.isEmpty();
        }

        Collection<List<LauncherActivityInfo>> lists() {
            return packages.values();
        }

        LauncherActivityInfo poll(AppDescriptor item) {
            List<LauncherActivityInfo> infos = packages.get(item.packageName);
            if (infos == null || infos.isEmpty()) {
                return null;
            }
            if (infos.size() == 1) {
                LauncherActivityInfo result = infos.remove(0);
                packages.remove(item.packageName);
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

        private static Map<String, List<LauncherActivityInfo>> groupByPackage(List<LauncherActivityInfo> infoList) {
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
