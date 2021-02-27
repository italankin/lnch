package com.italankin.lnch.model.backup;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Completable;

/**
 * A class which reads data from a backup resource
 */
public interface BackupReader {

    Completable read(Uri uri);

    Completable read(InputStreamFactory factory);

    interface InputStreamFactory {
        InputStream get() throws IOException;
    }
}
