package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.IntentUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.reactivex.Maybe;

import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.groupByPackage;

public class LoadFromFileInteractor {

    private final AppDescriptorInteractor appDescriptorInteractor;
    private final PackagesStore packagesStore;
    private final DescriptorStore descriptorStore;
    private final ShortcutsRepository shortcutsRepository;
    private final PackageManager packageManager;

    public LoadFromFileInteractor(AppDescriptorInteractor appDescriptorInteractor,
            PackagesStore packagesStore, DescriptorStore descriptorStore,
            ShortcutsRepository shortcutsRepository, PackageManager packageManager) {
        this.appDescriptorInteractor = appDescriptorInteractor;
        this.packagesStore = packagesStore;
        this.descriptorStore = descriptorStore;
        this.shortcutsRepository = shortcutsRepository;
        this.packageManager = packageManager;
    }

    public Maybe<AppsData> load(List<LauncherActivityInfo> infoList) {
        return Maybe
                .create(emitter -> {
                    try {
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
                        ProcessingEnv env = new ProcessingEnv(
                                shortcutsRepository.getPinnedShortcuts(),
                                new PackagesMap(groupByPackage(infoList)));

                        processSavedItems(env, savedItems);
                        processNewApps(env);
                        processShortcuts(env);

                        emitter.onSuccess(env.getData());
                    } catch (Exception e) {
                        emitter.tryOnError(e);
                    }
                });
    }

    private void processSavedItems(ProcessingEnv env, List<Descriptor> savedItems) {
        for (Descriptor item : savedItems) {
            if (item instanceof PinnedShortcutDescriptor) {
                visitPinnedShortcut(env, (PinnedShortcutDescriptor) item);
            } else if (item instanceof DeepShortcutDescriptor) {
                visitDeepShortcut(env, (DeepShortcutDescriptor) item);
            } else if (item instanceof AppDescriptor) {
                visitApp(env, (AppDescriptor) item);
            } else if (item instanceof IntentDescriptor) {
                visitIntent(env, (IntentDescriptor) item);
            } else {
                env.addItem(item);
            }
        }
    }

    private void processNewApps(ProcessingEnv env) {
        for (List<LauncherActivityInfo> infos : env.packages()) {
            if (infos.size() == 1) {
                AppDescriptor item = appDescriptorInteractor.createItem(infos.get(0));
                env.addItem(item);
            } else {
                for (LauncherActivityInfo info : infos) {
                    AppDescriptor item = appDescriptorInteractor.createItem(info, true);
                    env.addItem(item);
                }
            }
        }
    }

    private void processShortcuts(ProcessingEnv env) {
        for (Shortcut shortcut : env.shortcuts()) {
            String packageName = shortcut.getPackageName();
            DeepShortcutDescriptor item = new DeepShortcutDescriptor(
                    packageName, shortcut.getId());
            AppDescriptor app = env.findInstalled(packageName);
            assert app != null;
            item.color = app.color;
            String label = shortcut.getShortLabel().toString();
            if (TextUtils.isEmpty(label)) {
                item.label = app.getVisibleLabel();
            } else {
                item.label = label.toUpperCase(Locale.getDefault());
            }
            env.addItem(item);
        }
    }

    private void visitApp(ProcessingEnv env, AppDescriptor app) {
        LauncherActivityInfo info = env.pollInfo(app);
        if (info != null) {
            appDescriptorInteractor.updateItem(app, info);
            env.addItem(app);
        } else {
            env.addDeleted(app);
        }
    }

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor item) {
        env.addItem(item);
        if (env.isShortcutsEmpty()) {
            item.enabled = false;
            return;
        }
        for (Iterator<Shortcut> iter = env.shortcuts().iterator(); iter.hasNext(); ) {
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
            env.addItem(item);
        } else {
            env.addDeleted(item);
        }
    }

    private void visitIntent(ProcessingEnv env, IntentDescriptor item) {
        if (IntentUtils.canHandleIntent(packageManager, IntentUtils.fromUri(item.intentUri))) {
            env.addItem(item);
        } else {
            env.addDeleted(item);
        }
    }
}

/**
 * Processing environment which holds temporary data
 */
class ProcessingEnv {
    private final List<Descriptor> items = new ArrayList<>(64);
    private final PackagesMap packagesMap;
    private final List<Shortcut> shortcuts;
    private final Map<String, AppDescriptor> installed = new HashMap<>(64);
    private final Set<Integer> deleted = new HashSet<>(4);

    ProcessingEnv(List<Shortcut> shortcuts, PackagesMap packagesMap) {
        this.shortcuts = new ArrayList<>(shortcuts);
        this.packagesMap = packagesMap;
    }

    void addItem(Descriptor item) {
        this.items.add(item);
    }

    void addItem(AppDescriptor app) {
        this.items.add(app);
        this.installed.put(app.packageName, app);
    }

    void addDeleted(Descriptor descriptor) {
        this.deleted.add(descriptor.hashCode());
    }

    AppDescriptor findInstalled(String packageName) {
        return installed.get(packageName);
    }

    LauncherActivityInfo pollInfo(AppDescriptor app) {
        return packagesMap.poll(app);
    }

    boolean isShortcutsEmpty() {
        return shortcuts.isEmpty();
    }

    Iterable<List<LauncherActivityInfo>> packages() {
        return packagesMap.items();
    }

    Iterable<Shortcut> shortcuts() {
        return shortcuts;
    }

    AppsData getData() {
        boolean changed = !deleted.isEmpty() || !packagesMap.isEmpty()
                || !shortcuts.isEmpty();
        return new AppsData(items, changed);
    }
}

/**
 * A map which holds {@link LauncherActivityInfo} groupped by
 * {@link android.content.pm.ApplicationInfo#packageName}
 */
class PackagesMap {
    private final Map<String, List<LauncherActivityInfo>> packages;

    PackagesMap(Map<String, List<LauncherActivityInfo>> packages) {
        this.packages = packages;
    }

    boolean isEmpty() {
        return packages.isEmpty();
    }

    Iterable<List<LauncherActivityInfo>> items() {
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
}
