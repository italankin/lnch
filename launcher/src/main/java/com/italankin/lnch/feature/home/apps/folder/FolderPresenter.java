package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Iterator;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class FolderPresenter extends BaseFolderPresenter<FolderView> {

    @Inject
    FolderPresenter(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences) {
        super(homeDescriptorsState, descriptorRepository, preferences);
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
