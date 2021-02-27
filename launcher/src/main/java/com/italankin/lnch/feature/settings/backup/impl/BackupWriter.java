package com.italankin.lnch.feature.settings.backup.impl;

import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Completable;

public interface BackupWriter {
    Completable write(Uri uri);

    Completable write(OutputStreamFactory factory);

    interface OutputStreamFactory {
        OutputStream get() throws IOException;
    }
}
