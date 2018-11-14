package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import timber.log.Timber;

public class BackupDescriptorStore implements DescriptorStore {
    private final DescriptorStore delegate;

    public BackupDescriptorStore(DescriptorStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Descriptor> read(File packagesFile) {
        return delegate.read(packagesFile);
    }

    @Override
    public void write(File packagesFile, List<Descriptor> items) {
        try {
            writeBackup(packagesFile);
        } catch (Exception e) {
            Timber.e(e, "write:");
        }
        delegate.write(packagesFile, items);
    }

    public void writeBackup(File packagesFile) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(packagesFile);
            File packagesBackupFile = new File(packagesFile.getParentFile(), packagesFile.getName() + ".backup");
            os = new FileOutputStream(packagesBackupFile);
            byte[] buffer = new byte[16384];
            int count;
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            Timber.d("Created backup: %s", packagesBackupFile);
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
