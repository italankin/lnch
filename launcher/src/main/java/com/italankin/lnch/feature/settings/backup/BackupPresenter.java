package com.italankin.lnch.feature.settings.backup;

import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.backup.BackupReader;
import com.italankin.lnch.model.backup.BackupWriter;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class BackupPresenter extends AppPresenter<BackupView> {

    private final BackupReader backupReader;
    private final BackupWriter backupWriter;
    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    @Inject
    BackupPresenter(BackupReader backupReader,
            BackupWriter backupWriter,
            DescriptorRepository descriptorRepository,
            Preferences preferences) {
        this.backupReader = backupReader;
        this.backupWriter = backupWriter;
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    void onRestoreSettings(Uri uri) {
        backupReader.read(uri)
                .andThen(descriptorRepository.update())
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
                .andThen(descriptorRepository.update())
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

    void resetLnchSettings() {
        Completable
                .fromRunnable(() -> {
                    Preferences.Pref<?>[] prefs = Preferences.ALL.toArray(new Preferences.Pref<?>[0]);
                    preferences.reset(prefs);
                })
                .andThen(descriptorRepository.update())
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
