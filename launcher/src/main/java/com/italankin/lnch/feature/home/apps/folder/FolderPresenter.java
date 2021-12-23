package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.prefs.Preferences;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class FolderPresenter extends BaseFolderPresenter<FolderView> {

    @Inject
    FolderPresenter(DescriptorRepository descriptorRepository, Preferences preferences) {
        super(descriptorRepository, preferences);
    }

    void removeFromFolderImmediate(String descriptorId) {
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folder.getId(), descriptorId))
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
}
