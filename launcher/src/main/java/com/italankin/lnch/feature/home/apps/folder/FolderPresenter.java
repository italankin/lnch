package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class FolderPresenter extends AppPresenter<FolderView> {

    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final Preferences preferences;

    @Inject
    FolderPresenter(DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.preferences = preferences;
    }

    void loadFolder(String descriptorId) {
        Single
                .fromCallable(() -> {
                    return descriptorRepository.findById(FolderDescriptor.class, descriptorId);
                })
                .zipWith(descriptorsById(), (folder, descriptorsById) -> {
                    List<DescriptorUi> items = new ArrayList<>(4);
                    for (String id : folder.items) {
                        Descriptor descriptor = descriptorsById.get(id);
                        if (descriptor != null) {
                            DescriptorUi item = DescriptorUiFactory.createItem(descriptor);
                            ((InFolderDescriptorUi) item).setFolderId(folder.id);
                            items.add(item);
                        }
                    }
                    Collections.sort(items, new AscLabelComparator());
                    return new FolderState(folder, items, new UserPrefs(preferences));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<FolderState>() {
                    @Override
                    protected void onSuccess(FolderView viewState, FolderState folderState) {
                        viewState.onShowFolder(folderState.descriptor, folderState.items, folderState.userPrefs);
                    }

                    @Override
                    protected void onError(FolderView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    void pinShortcut(Shortcut shortcut) {
        shortcutsRepository.pinShortcut(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<Boolean>() {
                    @Override
                    protected void onSuccess(FolderView viewState, Boolean pinned) {
                        if (pinned) {
                            viewState.onShortcutPinned(shortcut);
                        } else {
                            viewState.onShortcutAlreadyPinnedError(shortcut);
                        }
                    }

                    @Override
                    protected void onError(FolderView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    void removeItemImmediate(RemovableDescriptorUi item) {
        Descriptor descriptor = item.getDescriptor();
        DescriptorRepository.Editor editor = descriptorRepository.edit();
        editor.enqueue(new RemoveAction(descriptor));
        editor.commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Item removed: %s", descriptor);
                    }
                });
    }

    void removeFromFolder(InFolderDescriptorUi item) {
        Single
                .fromCallable(() -> {
                    return descriptorRepository.findById(FolderDescriptor.class, item.getFolderId());
                })
                .flatMapCompletable(folder -> {
                    return descriptorRepository.edit()
                            .enqueue(new RemoveFromFolderAction(folder.id, item.getDescriptor().getId()))
                            .commit();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "removeFromFolder: %s", e.getMessage());
                    }
                });
    }

    private Single<Map<String, Descriptor>> descriptorsById() {
        return Single.fromCallable(() -> {
            List<Descriptor> items = descriptorRepository.items();
            HashMap<String, Descriptor> result = new HashMap<>(items.size());
            for (Descriptor item : items) {
                result.put(item.getId(), item);
            }
            return result;
        });
    }

    private static class FolderState {
        final FolderDescriptor descriptor;
        final List<DescriptorUi> items;
        final UserPrefs userPrefs;

        FolderState(FolderDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
            this.descriptor = descriptor;
            this.items = items;
            this.userPrefs = userPrefs;
        }
    }

    private static class AscLabelComparator implements Comparator<DescriptorUi> {
        @Override
        public int compare(DescriptorUi lhs, DescriptorUi rhs) {
            if (lhs instanceof CustomLabelDescriptorUi && rhs instanceof CustomLabelDescriptorUi) {
                String lhsLabel = ((CustomLabelDescriptorUi) lhs).getVisibleLabel();
                String rhsLabel = ((CustomLabelDescriptorUi) rhs).getVisibleLabel();
                return String.CASE_INSENSITIVE_ORDER.compare(lhsLabel, rhsLabel);
            } else {
                // should not happen
                return lhs.getDescriptor().getId().compareTo(rhs.getDescriptor().getId());
            }
        }
    }

    private static class RemoveFromFolderAction implements DescriptorRepository.Editor.Action {
        private final String folderId;
        private final String itemId;

        RemoveFromFolderAction(String folderId, String itemId) {
            this.folderId = folderId;
            this.itemId = itemId;
        }

        @Override
        public void apply(List<Descriptor> items) {
            for (Descriptor item : items) {
                if (item.getId().equals(folderId)) {
                    ((FolderDescriptor) item).items.remove(itemId);
                    break;
                }
            }
        }
    }
}
