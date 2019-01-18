package com.italankin.lnch.feature.settings.backup;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import com.italankin.lnch.model.repository.store.PackagesStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class BackupPresenter extends AppPresenter<BackupView> {

    private static final int BUFFER_SIZE = 16384;
    private static final String BACKUP_FILE_FORMAT = "lnch-backup-%s.json";
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @SuppressLint("ConstantLocale")
        @Override
        public DateFormat get() {
            return new SimpleDateFormat("yyyy_MM_dd-hh_mm_ss", Locale.getDefault());
        }
    };

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

    void onRestoreFromSource(Uri uri) {
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

    void onBackupSettings() {
        Single
                .<String>create(emitter -> {
                    File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(dirDownloads))) {
                        emitter.onError(new RuntimeException("External storage is not available"));
                        return;
                    }
                    String backupFileName = String.format(Locale.getDefault(),
                            BACKUP_FILE_FORMAT,
                            DATE_FORMAT.get().format(new Date()));
                    File backupFile = new File(dirDownloads, backupFileName);
                    try (InputStream is = packagesStore.input(); OutputStream os = new FileOutputStream(backupFile)) {
                        int read;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while ((read = is.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                    }
                    emitter.onSuccess(backupFile.getAbsolutePath());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<String>() {
                    @Override
                    protected void onSuccess(BackupView viewState, String path) {
                        viewState.onBackupSuccess(path);
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
