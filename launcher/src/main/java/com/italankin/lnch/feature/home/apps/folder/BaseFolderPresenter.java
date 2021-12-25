package com.italankin.lnch.feature.home.apps.folder;

import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

abstract class BaseFolderPresenter<V extends BaseFolderView> extends AppPresenter<V> {

    protected final HomeDescriptorsState homeDescriptorsState;
    protected final DescriptorRepository descriptorRepository;
    protected final Preferences preferences;

    protected FolderDescriptorUi folder;
    protected List<DescriptorUi> items;

    BaseFolderPresenter(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences) {
        this.homeDescriptorsState = homeDescriptorsState;
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    protected void loadFolder(String folderId) {
        Single
                .fromCallable(() -> {
                    HomeEntry<FolderDescriptorUi> entry = homeDescriptorsState.find(
                            FolderDescriptorUi.class, folderId);
                    if (entry == null) {
                        throw new NullPointerException("No folder found for folderId=" + folderId);
                    }
                    FolderDescriptorUi folder = entry.item;
                    List<DescriptorUi> items = new ArrayList<>(homeDescriptorsState.folderItems(folder));
                    if (items.isEmpty()) {
                        items.add(new EmptyFolderDescriptorUi());
                    }
                    return new FolderState(folder, items, new UserPrefs(preferences));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<FolderState>() {
                    @Override
                    public void onSuccess(BaseFolderView viewState, FolderState folderState) {
                        folder = folderState.folder;
                        items = folderState.items;
                        viewState.onShowFolder(
                                folderState.folder.getVisibleLabel(), folderState.items, folderState.userPrefs);
                    }

                    @Override
                    protected void onError(BaseFolderView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    static class FolderState {
        final FolderDescriptorUi folder;
        final List<DescriptorUi> items;
        final UserPrefs userPrefs;

        FolderState(FolderDescriptorUi folder, List<DescriptorUi> items, UserPrefs userPrefs) {
            this.folder = folder;
            this.items = items;
            this.userPrefs = userPrefs;
        }
    }
}
