package com.italankin.lnch.feature.settings.backup.impl;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Completable;

public interface BackupReader {
    Completable read(Uri uri);

    Completable read(InputStreamFactory factory);

    interface InputStreamFactory {
        InputStream get() throws IOException;
    }
}
