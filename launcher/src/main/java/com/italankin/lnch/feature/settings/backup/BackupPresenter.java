package com.italankin.lnch.feature.settings.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class BackupPresenter extends AppPresenter<BackupView> {

    private final ContentResolver contentResolver;
    private final DescriptorStore descriptorStore;
    private final DescriptorRepository descriptorRepository;

    @Inject
    BackupPresenter(Context context, DescriptorStore descriptorStore,
            DescriptorRepository descriptorRepository) {
        this.contentResolver = context.getContentResolver();
        this.descriptorStore = descriptorStore;
        this.descriptorRepository = descriptorRepository;
    }

    void onRestoreFromSource(Uri uri) {
        getViewState().showProgress();
        Single.fromCallable(() -> contentResolver.openInputStream(uri))
                .map(descriptorStore::read)
                .flatMapCompletable(descriptors -> {
                    return descriptorRepository.edit()
                            .enqueue(new ReplaceAction(descriptors))
                            .commit();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(BackupView viewState) {
                        viewState.onRestoreSuccess();
                    }

                    @Override
                    protected void onError(BackupView viewState, Throwable e) {
                        viewState.onRestoreError(e);
                    }
                });
    }

    private final class ReplaceAction implements DescriptorRepository.Editor.Action {
        private final List<Descriptor> newItems;

        private ReplaceAction(List<Descriptor> newItems) {
            this.newItems = newItems;
        }

        @Override
        public void apply(List<Descriptor> items) {
            items.clear();
            items.addAll(newItems);
        }
    }
}
