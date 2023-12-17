package com.italankin.lnch.feature.home.apps.folder;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

abstract class BaseFolderViewModel extends AppViewModel {

    protected final BehaviorSubject<FolderState> folderStateSubject = BehaviorSubject.create();
    protected final PublishSubject<List<DescriptorUi>> folderUpdateEvents = PublishSubject.create();

    protected final HomeDescriptorsState homeDescriptorsState;
    protected final DescriptorRepository descriptorRepository;
    protected final Preferences preferences;
    protected final FontManager fontManager;

    protected FolderDescriptorUi folder;
    protected List<DescriptorUi> items;

    BaseFolderViewModel(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences, FontManager fontManager) {
        this.homeDescriptorsState = homeDescriptorsState;
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
        this.fontManager = fontManager;
    }

    Observable<FolderState> showFolderEvents() {
        return folderStateSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<List<DescriptorUi>> folderUpdateEvents() {
        return folderUpdateEvents.observeOn(AndroidSchedulers.mainThread());
    }

    protected void loadFolder(String folderId, boolean animated) {
        Single
                .fromCallable(() -> {
                    HomeEntry<FolderDescriptorUi> entry = homeDescriptorsState.find(FolderDescriptorUi.class, folderId);
                    if (entry == null) {
                        throw new NullPointerException("No folder found for folderId=" + folderId);
                    }
                    FolderDescriptorUi folder = entry.item;
                    List<DescriptorUi> items = new ArrayList<>(homeDescriptorsState.folderItems(folder));
                    if (items.isEmpty()) {
                        items.add(new EmptyFolderDescriptorUi());
                    }
                    return new FolderState(folder, items, new UserPrefs(preferences, fontManager), animated);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(FolderState folderState) {
                        folder = folderState.folder;
                        items = folderState.items;
                        folderStateSubject.onNext(folderState);
                    }

                    @Override
                    public void onError(Throwable e) {
                        folderStateSubject.onError(e);
                    }
                });
    }
}
