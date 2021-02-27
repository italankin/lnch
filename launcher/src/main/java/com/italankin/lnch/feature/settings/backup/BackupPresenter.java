package com.italankin.lnch.feature.settings.backup;

import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.backup.BackupReader;
import com.italankin.lnch.model.backup.BackupWriter;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class BackupPresenter extends AppPresenter<BackupView> {

    private final BackupReader backupReader;
    private final BackupWriter backupWriter;
    private final DescriptorRepository descriptorRepository;

    @Inject
    BackupPresenter(BackupReader backupReader,
            BackupWriter backupWriter,
            DescriptorRepository descriptorRepository) {
        this.backupReader = backupReader;
        this.backupWriter = backupWriter;
        this.descriptorRepository = descriptorRepository;
    }

    void onRestoreSettings(Uri uri) {
        backupReader.read(uri)
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
        backupWriter.write(uri)
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
}
