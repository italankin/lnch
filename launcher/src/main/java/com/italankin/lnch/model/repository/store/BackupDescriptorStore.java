package com.italankin.lnch.model.repository.store;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import timber.log.Timber;

public class BackupDescriptorStore implements DescriptorStore {

    private static final String BACKUP_FILE_NAME = "packages.json.backup";
    private static final int BUFFER_SIZE = 16384;

    private final File parentDir;
    private final DescriptorStore delegate;
    private final PackagesStore packagesStore;

    public BackupDescriptorStore(DescriptorStore delegate, PackagesStore packagesStore, File parentDir) {
        this.parentDir = parentDir;
        this.delegate = delegate;
        this.packagesStore = packagesStore;
    }

    @Override
    public List<Descriptor> read(InputStream in) {
        return delegate.read(in);
    }

    @Override
    public void write(OutputStream out, List<Descriptor> items) {
        try {
            createBackup();
        } catch (Exception e) {
            Timber.e(e, "write:");
        }
        delegate.write(out, items);
    }

    private void createBackup() throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = packagesStore.input();
            if (is == null) {
                return;
            }
            File backup = new File(parentDir, BACKUP_FILE_NAME);
            os = new FileOutputStream(backup);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            Timber.d("created backup: %s", backup);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
}
