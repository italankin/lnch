package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.IntentUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Maybe;
import timber.log.Timber;

import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.groupByPackage;

public class LoadFromFileInteractor {

    private final AppDescriptorInteractor appDescriptorInteractor;
    private final PackagesStore packagesStore;
    private final DescriptorStore descriptorStore;
    private final ShortcutsRepository shortcutsRepository;
    private final PackageManager packageManager;
    private final NameNormalizer nameNormalizer;

    public LoadFromFileInteractor(AppDescriptorInteractor appDescriptorInteractor,
            PackagesStore packagesStore, DescriptorStore descriptorStore,
            ShortcutsRepository shortcutsRepository, PackageManager packageManager,
            NameNormalizer nameNormalizer) {
        this.appDescriptorInteractor = appDescriptorInteractor;
        this.packagesStore = packagesStore;
        this.descriptorStore = descriptorStore;
        this.shortcutsRepository = shortcutsRepository;
        this.packageManager = packageManager;
        this.nameNormalizer = nameNormalizer;
    }

    /**
     * Load saved descriptor data from disk. Signals empty, if no saved data found or the data is corrupt.
     */
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
                        ProcessingEnv env = new ProcessingEnv(new PackagesMap(groupByPackage(infoList)));

                        processSavedItems(env, savedItems);
                        processNewApps(env);
                        cleanupFolders(env);

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
            } else if (item instanceof FolderDescriptor) {
                visitFolder(env, (FolderDescriptor) item);
            } else {
                env.addUnknown(item);
            }
        }
    }

    private void processNewApps(ProcessingEnv env) {
        for (List<LauncherActivityInfo> infos : env.packages()) {
            if (infos.size() == 1) {
                AppDescriptor item = appDescriptorInteractor.createItem(infos.get(0));
                env.addApp(item);
            } else {
                for (LauncherActivityInfo info : infos) {
                    AppDescriptor item = appDescriptorInteractor.createItem(info, true);
                    env.addApp(item);
                }
            }
        }
    }

    private void cleanupFolders(ProcessingEnv env) {
        Set<String> itemIds = env.itemIds();
        for (FolderDescriptor folder : env.folders()) {
            Iterator<String> iterator = folder.items.iterator();
            boolean changed = false;
            while (iterator.hasNext()) {
                String folderItemId = iterator.next();
                if (!itemIds.contains(folderItemId)) {
                    Timber.d("remove unavailable '%s' from '%s'", folderItemId, folder.id);
                    iterator.remove();
                    changed = true;
                }
            }
            if (changed) {
                env.markUpdated(folder);
            }
        }
    }

    private void visitApp(ProcessingEnv env, AppDescriptor app) {
        LauncherActivityInfo info = env.pollInfo(app);
        if (info != null) {
            if (appDescriptorInteractor.updateItem(app, info)) {
                env.markUpdated(app);
            }
            env.addApp(app);
        } else {
            env.markDeleted(app);
        }
    }

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor item) {
        Shortcut shortcut = shortcutsRepository.getShortcut(item.packageName, item.id);
        item.enabled = shortcut != null;
        String originalLabel = item.getOriginalLabel();
        if (originalLabel != null) {
            item.label = nameNormalizer.normalize(originalLabel);
        } else {
            if (shortcut != null) {
                item.originalLabel = shortcut.getShortLabel().toString();
                item.label = nameNormalizer.normalize(item.getOriginalLabel());
            } else {
                item.originalLabel = item.label;
            }
            env.markUpdated(item);
        }
        env.addDeepShortcut(item);
    }

    private void visitPinnedShortcut(ProcessingEnv env, PinnedShortcutDescriptor item) {
        String uri = item.uri;
        Intent intent = IntentUtils.fromUri(uri);
        if (IntentUtils.canHandleIntent(packageManager, intent)) {
            String originalLabel = item.getOriginalLabel();
            if (originalLabel == null) {
                item.originalLabel = item.label;
                env.markUpdated(item);
            }
            item.label = nameNormalizer.normalize(item.getOriginalLabel());
            env.addPinnedShortcut(item);
        } else {
            env.markDeleted(item);
        }
    }

    private void visitIntent(ProcessingEnv env, IntentDescriptor item) {
        if (IntentUtils.canHandleIntent(packageManager, IntentUtils.fromUri(item.intentUri))) {
            String originalLabel = item.getOriginalLabel();
            if (originalLabel == null) {
                item.originalLabel = item.label;
                env.markUpdated(item);
            }
            item.label = nameNormalizer.normalize(item.getOriginalLabel());
            env.addIntent(item);
        } else {
            env.markDeleted(item);
        }
    }

    private void visitFolder(ProcessingEnv env, FolderDescriptor item) {
        String originalLabel = item.getOriginalLabel();
        if (originalLabel == null) {
            item.originalLabel = item.label;
            env.markUpdated(item);
        }
        item.label = nameNormalizer.normalize(item.getOriginalLabel());
        env.addFolder(item);
    }
}

/**
 * Processing environment object which holds temporary data
 */
class ProcessingEnv {
    private final List<Descriptor> items = new ArrayList<>(64);
    private final Set<String> itemIds = new HashSet<>(64);
    private final List<FolderDescriptor> folders = new ArrayList<>(4);
    /**
     * Packages data, fetched from {@link android.content.pm.LauncherApps}
     */
    private final PackagesMap packagesMap;
    private int deleted = 0;
    private int updated = 0;

    ProcessingEnv(PackagesMap packagesMap) {
        this.packagesMap = packagesMap;
    }

    private void addItem(Descriptor item) {
        this.items.add(item);
        this.itemIds.add(item.getId());
    }

    void addApp(AppDescriptor item) {
        addItem(item);
    }

    void addIntent(IntentDescriptor item) {
        addItem(item);
    }

    void addFolder(FolderDescriptor item) {
        addItem(item);
        folders.add(item);
    }

    void addDeepShortcut(DeepShortcutDescriptor item) {
        addItem(item);
    }

    void addPinnedShortcut(PinnedShortcutDescriptor item) {
        addItem(item);
    }

    void addUnknown(Descriptor item) {
        Timber.e("addUnknown: class=%s, item=%s", item.getClass(), item);
        addItem(item);
    }

    void markDeleted(Descriptor descriptor) {
        Timber.d("markDeleted: %s", descriptor);
        deleted++;
    }

    void markUpdated(Descriptor descriptor) {
        Timber.d("markUpdated: %s", descriptor);
        updated++;
    }

    /**
     * Remove from a {@code packagesMap} and return matching {@link LauncherActivityInfo}
     * for a given {@link AppDescriptor}
     */
    LauncherActivityInfo pollInfo(AppDescriptor app) {
        return packagesMap.poll(app);
    }

    /**
     * Remaining items in the {@code packagesMap} will be newly installed apps
     */
    Iterable<List<LauncherActivityInfo>> packages() {
        return packagesMap.items();
    }

    Set<String> itemIds() {
        return itemIds;
    }

    List<FolderDescriptor> folders() {
        return folders;
    }

    AppsData getData() {
        boolean changed = deleted > 0 || updated > 0 || !packagesMap.isEmpty();
        return new AppsData(items, changed);
    }
}

/**
 * A map which holds {@link LauncherActivityInfo} grouped by {@link android.content.pm.ApplicationInfo#packageName}
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

    /**
     * Get matching {@link LauncherActivityInfo} for a given {@link AppDescriptor}.
     * If {@code null} is returned, the app probably got deleted.
     */
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
