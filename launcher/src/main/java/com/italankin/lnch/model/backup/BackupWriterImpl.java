package com.italankin.lnch.model.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import io.reactivex.Completable;
import timber.log.Timber;

public class BackupWriterImpl implements BackupWriter {

    private final ContentResolver contentResolver;
    private final Gson gson;
    private final DescriptorRepository descriptorRepository;
    private final PreferencesBackup preferencesBackup;

    public BackupWriterImpl(Context context,
            GsonBuilder gsonBuilder,
            DescriptorRepository descriptorRepository,
            PreferencesBackup preferencesBackup) {
        this.contentResolver = context.getContentResolver();
        this.gson = gsonBuilder.create();
        this.descriptorRepository = descriptorRepository;
        this.preferencesBackup = preferencesBackup;
    }

    @Override
    public Completable write(Uri uri) {
        return write(() -> contentResolver.openOutputStream(uri));
    }

    @Override
    public Completable write(OutputStreamFactory factory) {
        return Completable.using(
                factory::get,
                outputStream -> {
                    return Completable.fromRunnable(() -> {
                        Map<String, Object> preferences = preferencesBackup.read();
                        List<Descriptor> descriptors = descriptorRepository.items();
                        Backup backup = new Backup(descriptors, preferences);
                        Timber.d("create backup:\n%s", backup);
                        try (OutputStreamWriter writer = getWriter(outputStream)) {
                            gson.toJson(backup, writer);
                        } catch (IOException e) {
                            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
                        }
                    });
                },
                BackupUtils::closeQuietly);
    }

    private OutputStreamWriter getWriter(OutputStream outputStream) throws IOException {
        return new OutputStreamWriter(new GZIPOutputStream(outputStream, BackupUtils.DEFAULT_BUFFER_SIZE));
    }
}
