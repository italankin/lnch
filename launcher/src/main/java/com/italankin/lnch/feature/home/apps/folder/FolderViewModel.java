package com.italankin.lnch.feature.home.apps.folder;

import androidx.annotation.NonNull;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

public class FolderViewModel extends BaseFolderViewModel {

    private final HomeDescriptorsState.Callback stateCallback = new HomeDescriptorsState.Callback() {
        @Override
        public void onNewItems(List<DescriptorUi> items) {
            loadFolder(folder.getDescriptor().id, false);
        }
    };

    @Inject
    FolderViewModel(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences, FontManager fontManager) {
        super(homeDescriptorsState, descriptorRepository, preferences, fontManager);
        homeDescriptorsState.addCallback(stateCallback);
    }

    void removeFromFolderImmediate(String descriptorId) {
        removeItemFromFolder(descriptorId);
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folder.getDescriptor().getId(), descriptorId))
                .commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "removeFromFolderImmediate: %s", e.getMessage());
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        homeDescriptorsState.removeCallback(stateCallback);
    }

    private void removeItemFromFolder(String descriptorId) {
        for (Iterator<DescriptorUi> i = items.iterator(); i.hasNext(); ) {
            if (i.next().getDescriptor().getId().equals(descriptorId)) {
                i.remove();
                break;
            }
        }
        if (items.isEmpty()) {
            items.add(new EmptyFolderDescriptorUi());
        }
        folderUpdateEvents.onNext(items);
    }
}
