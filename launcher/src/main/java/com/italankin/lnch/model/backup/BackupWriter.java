package com.italankin.lnch.model.backup;

import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Completable;

/**
 * A class which dumps current state of descriptors and preferences to a resource (e.g. file)
 */
public interface BackupWriter {

    Completable write(Uri uri);

    Completable write(OutputStreamFactory factory);

    interface OutputStreamFactory {
        OutputStream get() throws IOException;
    }
}
