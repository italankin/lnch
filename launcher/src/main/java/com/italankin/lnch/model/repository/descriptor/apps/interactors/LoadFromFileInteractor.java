package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.*;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.apps.AppsData;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;
import com.italankin.lnch.util.IntentUtils;
import io.reactivex.Maybe;
import timber.log.Timber;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

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
                        File packagesFile = packagesStore.input();
                        if (packagesFile == null) {
                            emitter.onComplete();
                            return;
                        }
                        List<Descriptor> savedItems;
                        try (FileInputStream fis = new FileInputStream(packagesFile)) {
                            savedItems = descriptorStore.read(fis);
                        }
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
                visitPinnedShortcut(env, ((PinnedShortcutDescriptor) item).toMutable());
            } else if (item instanceof DeepShortcutDescriptor) {
                visitDeepShortcut(env, ((DeepShortcutDescriptor) item).toMutable());
            } else if (item instanceof AppDescriptor) {
                visitApp(env, ((AppDescriptor) item).toMutable());
            } else if (item instanceof IntentDescriptor) {
                visitIntent(env, ((IntentDescriptor) item).toMutable());
            } else if (item instanceof FolderDescriptor) {
                visitFolder(env, ((FolderDescriptor) item).toMutable());
            } else {
                env.addUnknown(item.toMutable());
            }
        }
    }

    private void processNewApps(ProcessingEnv env) {
        for (List<LauncherActivityInfo> infos : env.packages()) {
            if (infos.size() == 1) {
                AppDescriptor.Mutable item = appDescriptorInteractor.createItem(infos.get(0));
                env.addApp(item);
            } else {
                for (LauncherActivityInfo info : infos) {
                    AppDescriptor.Mutable item = appDescriptorInteractor.createItem(info, true);
                    env.addApp(item);
                }
            }
        }
    }

    private void cleanupFolders(ProcessingEnv env) {
        Set<String> itemIds = env.itemIds();
        for (FolderDescriptor.Mutable folder : env.folders()) {
            Iterator<String> iterator = folder.getItems().iterator();
            boolean changed = false;
            while (iterator.hasNext()) {
                String folderItemId = iterator.next();
                if (!itemIds.contains(folderItemId)) {
                    Timber.d("remove unavailable '%s' from '%s'", folderItemId, folder.getId());
                    iterator.remove();
                    changed = true;
                }
            }
            if (changed) {
                env.markUpdated(folder);
            }
        }
    }

    private void visitApp(ProcessingEnv env, AppDescriptor.Mutable app) {
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

    private void visitDeepShortcut(ProcessingEnv env, DeepShortcutDescriptor.Mutable item) {
        Shortcut shortcut = shortcutsRepository.getShortcut(item.getPackageName(), item.getShortcutId());
        item.setEnabled(shortcut != null);
        String originalLabel = item.getOriginalLabel();
        if (originalLabel != null) {
            item.setLabel(nameNormalizer.normalize(originalLabel));
        } else {
            if (shortcut != null) {
                item.setOriginalLabel(shortcut.getShortLabel().toString());
                item.setLabel(nameNormalizer.normalize(item.getOriginalLabel()));
            } else {
                item.setOriginalLabel(item.getLabel());
            }
            env.markUpdated(item);
        }
        env.addDeepShortcut(item);
    }

    private void visitPinnedShortcut(ProcessingEnv env, PinnedShortcutDescriptor.Mutable item) {
        String uri = item.getUri();
        Intent intent = IntentUtils.fromUri(uri);
        if (IntentUtils.canHandleIntent(packageManager, intent)) {
            String originalLabel = item.getOriginalLabel();
            if (originalLabel == null) {
                item.setOriginalLabel(item.getLabel());
                env.markUpdated(item);
            }
            item.setLabel(nameNormalizer.normalize(item.getOriginalLabel()));
            env.addPinnedShortcut(item);
        } else {
            env.markDeleted(item);
        }
    }

    private void visitIntent(ProcessingEnv env, IntentDescriptor.Mutable item) {
        if (IntentUtils.canHandleIntent(packageManager, IntentUtils.fromUri(item.getIntentUri()))) {
            String originalLabel = item.getOriginalLabel();
            if (originalLabel == null) {
                item.setOriginalLabel(item.getLabel());
                env.markUpdated(item);
            }
            item.setLabel(nameNormalizer.normalize(item.getOriginalLabel()));
            env.addIntent(item);
        } else {
            env.markDeleted(item);
        }
    }

    private void visitFolder(ProcessingEnv env, FolderDescriptor.Mutable item) {
        String originalLabel = item.getOriginalLabel();
        if (originalLabel == null) {
            item.setOriginalLabel(item.getLabel());
            env.markUpdated(item);
        }
        item.setLabel(nameNormalizer.normalize(item.getOriginalLabel()));
        env.addFolder(item);
    }
}

/**
 * Processing environment object which holds temporary data
 */
class ProcessingEnv {
    private final List<MutableDescriptor<?>> items = new ArrayList<>(64);
    private final Set<String> itemIds = new HashSet<>(64);
    private final List<FolderDescriptor.Mutable> folders = new ArrayList<>(4);
    /**
     * Packages data, fetched from {@link android.content.pm.LauncherApps}
     */
    private final PackagesMap packagesMap;
    private int deleted = 0;
    private int updated = 0;

    ProcessingEnv(PackagesMap packagesMap) {
        this.packagesMap = packagesMap;
    }

    private void addItem(MutableDescriptor<?> item) {
        this.items.add(item);
        this.itemIds.add(item.getId());
    }

    void addApp(AppDescriptor.Mutable item) {
        addItem(item);
    }

    void addIntent(IntentDescriptor.Mutable item) {
        addItem(item);
    }

    void addFolder(FolderDescriptor.Mutable item) {
        addItem(item);
        folders.add(item);
    }

    void addDeepShortcut(DeepShortcutDescriptor.Mutable item) {
        addItem(item);
    }

    void addPinnedShortcut(PinnedShortcutDescriptor.Mutable item) {
        addItem(item);
    }

    void addUnknown(MutableDescriptor<?> item) {
        Timber.e("addUnknown: class=%s, item=%s", item.getClass(), item);
        addItem(item);
    }

    void markDeleted(MutableDescriptor<?> descriptor) {
        Timber.d("markDeleted: %s", descriptor);
        deleted++;
    }

    void markUpdated(MutableDescriptor<?> descriptor) {
        Timber.d("markUpdated: %s", descriptor);
        updated++;
    }

    /**
     * Remove from a {@code packagesMap} and return matching {@link LauncherActivityInfo}
     * for a given {@link AppDescriptor}
     */
    LauncherActivityInfo pollInfo(AppDescriptor.Mutable app) {
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

    List<FolderDescriptor.Mutable> folders() {
        return folders;
    }

    AppsData getData() {
        boolean changed = deleted > 0 || updated > 0 || !packagesMap.isEmpty();
        return AppsData.create(items, changed);
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
    LauncherActivityInfo poll(AppDescriptor.Mutable item) {
        List<LauncherActivityInfo> infos = packages.get(item.getPackageName());
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        if (infos.size() == 1) {
            LauncherActivityInfo result = infos.remove(0);
            packages.remove(item.getPackageName());
            return result;
        } else if (item.getComponentName() != null) {
            Iterator<LauncherActivityInfo> iter = infos.iterator();
            while (iter.hasNext()) {
                LauncherActivityInfo info = iter.next();
                String componentName = getComponentName(info);
                if (componentName.equals(item.getComponentName())) {
                    iter.remove();
                    return info;
                }
            }
        }
        return null;
    }
}
