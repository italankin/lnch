package com.italankin.lnch.feature.settings.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class BackupPresenter extends AppPresenter<BackupView> {

    private static final int BUFFER_SIZE = 16384;

    private final ContentResolver contentResolver;
    private final DescriptorStore descriptorStore;
    private final DescriptorRepository descriptorRepository;
    private final PackagesStore packagesStore;

    @Inject
    BackupPresenter(Context context, DescriptorStore descriptorStore,
            DescriptorRepository descriptorRepository, PackagesStore packagesStore) {
        this.contentResolver = context.getContentResolver();
        this.descriptorStore = descriptorStore;
        this.descriptorRepository = descriptorRepository;
        this.packagesStore = packagesStore;
    }

    void onRestoreSettings(Uri uri) {
        Single
                .<List<Descriptor>>create(emitter -> {
                    try (InputStream is = contentResolver.openInputStream(uri)) {
                        List<Descriptor> descriptors = descriptorStore.read(is);
                        if (descriptors == null) {
                            emitter.tryOnError(new IllegalArgumentException());
                            return;
                        }
                        emitter.onSuccess(descriptors);
                    } catch (Exception e) {
                        emitter.tryOnError(e);
                    }
                })
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

    void onBackupSettings(Uri uri) {
        Completable
                .create(emitter -> {
                    try (InputStream is = packagesStore.input(); OutputStream os = contentResolver.openOutputStream(uri)) {
                        if (os == null) {
                            emitter.tryOnError(new IllegalArgumentException());
                            return;
                        }
                        int read;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while ((read = is.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    } catch (Exception e) {
                        emitter.tryOnError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(BackupView viewState) {
                        viewState.onBackupSuccess();
                    }

                    @Override
                    protected void onError(BackupView viewState, Throwable e) {
                        viewState.onBackupError(e);
                    }
                });
    }

    void resetAppsSettings() {
        descriptorRepository.clear()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(BackupView viewState) {
                        viewState.onResetSuccess();
                    }

                    @Override
                    protected void onError(BackupView viewState, Throwable e) {
                        viewState.onResetError(e);
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
