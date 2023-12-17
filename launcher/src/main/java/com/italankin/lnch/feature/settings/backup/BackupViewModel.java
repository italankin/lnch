package com.italankin.lnch.feature.settings.backup;

import android.net.Uri;
import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.settings.backup.events.BackupEvent;
import com.italankin.lnch.feature.settings.backup.events.ResetEvent;
import com.italankin.lnch.feature.settings.backup.events.RestoreEvent;
import com.italankin.lnch.model.backup.BackupReader;
import com.italankin.lnch.model.backup.BackupWriter;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import javax.inject.Inject;

public class BackupViewModel extends AppViewModel {

    private final BackupReader backupReader;
    private final BackupWriter backupWriter;
    private final DescriptorRepository descriptorRepository;
    private final FontManager fontManager;
    private final Preferences preferences;

    private final PublishSubject<RestoreEvent> restoreEventsSubject = PublishSubject.create();
    private final PublishSubject<BackupEvent> backupEventsSubject = PublishSubject.create();
    private final PublishSubject<ResetEvent> resetEventsSubject = PublishSubject.create();

    @Inject
    BackupViewModel(BackupReader backupReader,
            BackupWriter backupWriter,
            DescriptorRepository descriptorRepository,
            FontManager fontManager,
            Preferences preferences) {
        this.backupReader = backupReader;
        this.backupWriter = backupWriter;
        this.descriptorRepository = descriptorRepository;
        this.fontManager = fontManager;
        this.preferences = preferences;
    }

    Observable<ResetEvent> resetEvents() {
        return resetEventsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<RestoreEvent> restoreEvents() {
        return restoreEventsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<BackupEvent> backupEvents() {
        return backupEventsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void onRestoreSettings(Uri uri) {
        backupReader.read(uri)
                .andThen(descriptorRepository.update())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        restoreEventsSubject.onNext(RestoreEvent.SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        restoreEventsSubject.onNext(RestoreEvent.ERROR);
                    }
                });
    }

    void onBackupSettings(Uri uri) {
        backupWriter.write(uri)
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        backupEventsSubject.onNext(BackupEvent.SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        backupEventsSubject.onNext(BackupEvent.ERROR);
                    }
                });
    }

    void resetAppsSettings() {
        descriptorRepository.clear()
                .andThen(descriptorRepository.update())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        resetEventsSubject.onNext(ResetEvent.SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        resetEventsSubject.onNext(ResetEvent.ERROR);
                    }
                });
    }

    void resetLnchSettings() {
        Completable
                .fromRunnable(() -> {
                    Preferences.Pref<?>[] prefs = Preferences.ALL.toArray(new Preferences.Pref<?>[0]);
                    preferences.reset(prefs);
                })
                .andThen(fontManager.clear())
                .andThen(descriptorRepository.update())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        resetEventsSubject.onNext(ResetEvent.SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        resetEventsSubject.onNext(ResetEvent.ERROR);
                    }
                });
    }
}
