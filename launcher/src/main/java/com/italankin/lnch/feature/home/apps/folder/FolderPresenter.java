package com.italankin.lnch.feature.home.apps.folder;

import androidx.annotation.NonNull;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class FolderPresenter extends BaseFolderPresenter<FolderView> {

    private final HomeDescriptorsState.Callback stateCallback = new HomeDescriptorsState.Callback() {
        @Override
        public void onNewItems(List<DescriptorUi> items) {
            loadFolder(folder.getDescriptor().id, false);
        }
    };

    @Inject
    FolderPresenter(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences, FontManager fontManager) {
        super(homeDescriptorsState, descriptorRepository, preferences, fontManager);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        homeDescriptorsState.addCallback(stateCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeDescriptorsState.removeCallback(stateCallback);
    }

    void removeFromFolderImmediate(String descriptorId) {
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folder.getDescriptor().getId(), descriptorId))
                .commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        removeItemFromFolder(descriptorId);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "removeFromFolderImmediate: %s", e.getMessage());
                    }
                });
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
        getViewState().onFolderUpdated(items);
    }
}
