package com.italankin.lnch.feature.settings.backup.impl;

import android.net.Uri;

import io.reactivex.Completable;

public interface BackupWriter {
    Completable write(Uri uri);
}
