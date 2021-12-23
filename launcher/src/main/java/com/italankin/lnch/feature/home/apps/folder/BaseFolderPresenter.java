package com.italankin.lnch.feature.home.apps.folder;

import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

abstract class BaseFolderPresenter<V extends BaseFolderView> extends AppPresenter<V> {

    protected final DescriptorRepository descriptorRepository;
    protected final Preferences preferences;

    protected FolderDescriptor folder;
    protected List<DescriptorUi> items;

    BaseFolderPresenter(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    void loadFolder(String folderId) {
        Single
                .fromCallable(() -> {
                    return descriptorRepository.findById(FolderDescriptor.class, folderId);
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
                    if (items.isEmpty()) {
                        items.add(new EmptyFolderDescriptorUi());
                    }
                    return new FolderState(folder, items, new UserPrefs(preferences));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<FolderState>() {
                    @Override
                    public void onSuccess(FolderState folderState) {
                        folder = folderState.descriptor;
                        items = folderState.items;
                        getViewState().onShowFolder(folderState.descriptor, folderState.items, folderState.userPrefs);
                    }

                    @Override
                    protected void onError(BaseFolderView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    protected void removeItemFromFolder(String descriptorId) {
        for (Iterator<DescriptorUi> i = items.iterator(); i.hasNext(); ) {
            if (i.next().getDescriptor().getId().equals(descriptorId)) {
                i.remove();
                break;
            }
        }
        if (items.isEmpty()) {
            items.add(new EmptyFolderDescriptorUi());
        }
        getViewState().onFolderUpdated(items);
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

    protected static class FolderState {
        final FolderDescriptor descriptor;
        final List<DescriptorUi> items;
        final UserPrefs userPrefs;

        FolderState(FolderDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
            this.descriptor = descriptor;
            this.items = items;
            this.userPrefs = userPrefs;
        }
    }
}
