package com.italankin.lnch.model.repository.store.json;

import com.italankin.lnch.model.repository.store.PackagesStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class JsonPackagesStore implements PackagesStore {

    private static final String FILE_NAME = "packages.json";

    private final File parentDir;

    public JsonPackagesStore(File parentDir) {
        this.parentDir = parentDir;
    }

    @Override
    public InputStream input() {
        try {
            File file = getFile();
            if (!file.exists()) {
                Timber.w("file %s does not exist", file);
                return null;
            }
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Timber.e(e, "input:");
            return null;
        }
    }

    @Override
    public OutputStream output() {
        try {
            return new FileOutputStream(getFile());
        } catch (FileNotFoundException e) {
            Timber.e(e, "output:");
            return null;
        }
    }

    @Override
    public void clear() {
        File file = getFile();
        if (file.exists()) {
            if (file.delete()) {
                Timber.d("deleted file: %s", file);
            } else {
                Timber.d("could not delete file: %s", file);
            }
        }
    }

    private File getFile() {
        return new File(parentDir, FILE_NAME);
    }
}
