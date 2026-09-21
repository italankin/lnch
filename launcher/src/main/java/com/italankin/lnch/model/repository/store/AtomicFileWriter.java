package com.italankin.lnch.model.repository.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import timber.log.Timber;

public final class AtomicFileWriter {

    public static void write(File destination, Writer writer) throws IOException {
        File target = destination.getAbsoluteFile();
        File temporary = File.createTempFile(target.getName() + ".", ".tmp", target.getParentFile());
        try {
            try (FileOutputStream output = new FileOutputStream(temporary)) {
                writer.write(output);
                output.getFD().sync();
            }
            if (!temporary.renameTo(target)) {
                throw new IOException("cannot replace " + target + " with " + temporary);
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) {
                Timber.w("cannot delete temporary file: %s", temporary);
            }
        }
    }

    public interface Writer {
        void write(OutputStream output) throws IOException;
    }

    private AtomicFileWriter() {
    }
}
