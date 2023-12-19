package com.italankin.lnch.model.repository.store.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.repository.store.PackagesStore;
import timber.log.Timber;

import java.io.File;

public class JsonPackagesStore implements PackagesStore {

    private static final String FILE_NAME = "packages.json";

    private final File parentDir;

    public JsonPackagesStore(File parentDir) {
        this.parentDir = parentDir;
    }

    @Override
    @Nullable
    public File input() {
        File file = getFile();
        if (!file.exists()) {
            Timber.w("file %s does not exist", file);
            return null;
        }
        return file;
    }

    @NonNull
    @Override
    public File output() {
        return getFile();
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
