package com.italankin.lnch.model.repository.usage;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.DescriptorUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class UsageTrackerImpl implements UsageTracker {

    private static final String FILE_NAME = "usage.json";

    private final File usageJson;
    private final DescriptorRepository descriptorRepository;
    private final Gson gson;

    private final ConcurrentMap<String, Integer> launches = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean needRead = new AtomicBoolean(true);

    public UsageTrackerImpl(Context context, DescriptorRepository descriptorRepository, Gson gson) {
        this.usageJson = new File(context.getFilesDir(), FILE_NAME);
        this.descriptorRepository = descriptorRepository;
        this.gson = gson;
    }

    @Override
    public void trackLaunch(Descriptor descriptor) {
        readState();
        String id = descriptor.getId();
        Integer value = launches.get(id);
        if (value == null) {
            launches.put(id, 1);
        } else {
            launches.replace(id, value + 1);
        }
        writeState();
    }

    @Override
    public void trackShortcut(Shortcut shortcut) {
        readState();
        // TODO track shortcuts
    }

    @Override
    public List<String> getMostUsed() {
        readState();
        return sortedByValue(launches);
    }

    @Override
    public void clearStatistics() {
        this.launches.clear();
        writeState();
    }

    @NonNull
    private static <T> List<T> sortedByValue(Map<T, Integer> map) {
        List<Map.Entry<T, Integer>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, (lhs, rhs) -> {
            return rhs.getValue().compareTo(lhs.getValue());
        });
        List<T> result = new ArrayList<>(entries.size());
        for (Map.Entry<T, Integer> entry : entries) {
            result.add(entry.getKey());
        }
        return result;
    }

    private void readState() {
        if (needRead.compareAndSet(true, false)) {
            launches.clear();
            if (!usageJson.exists()) {
                return;
            }
            try (FileReader reader = new FileReader(usageJson)) {
                UsageStats stats = gson.fromJson(reader, UsageStats.class);
                if (stats.launches != null) {
                    Map<String, Descriptor> descriptors = DescriptorUtils.associateById(descriptorRepository.items());
                    for (Map.Entry<String, Integer> entry : stats.launches.entrySet()) {
                        Descriptor descriptor = descriptors.get(entry.getKey());
                        if (descriptor != null) {
                            launches.put(descriptor.getId(), entry.getValue());
                        }
                    }
                }
                Timber.d("read: %s", stats);
            } catch (Throwable e) {
                usageJson.delete();
                Timber.e(e, "read:");
            }
        }
    }

    private void writeState() {
        executor.execute(new WriteRunnable(usageJson, new HashMap<>(this.launches)));
    }

    private class WriteRunnable implements Runnable {
        private final File file;
        private final Map<String, Integer> descriptors;

        WriteRunnable(File file, Map<String, Integer> descriptors) {
            this.file = file;
            this.descriptors = descriptors;
        }

        @Override
        public void run() {
            Map<String, Descriptor> allDescriptors = DescriptorUtils.associateById(descriptorRepository.items());
            UsageStats stats = new UsageStats();
            stats.launches = new HashMap<>(descriptors.size());
            for (Map.Entry<String, Integer> entry : descriptors.entrySet()) {
                String descriptorId = entry.getKey();
                if (allDescriptors.containsKey(descriptorId)) {
                    stats.launches.put(descriptorId, entry.getValue());
                }
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(stats, writer);
                Timber.d("wrote: %s", stats);
            } catch (IOException e) {
                Timber.e(e, "write:");
            }
        }
    }

    private static class UsageStats {
        @SerializedName("descriptors")
        Map<String, Integer> launches;

        @NonNull
        @Override
        public String toString() {
            return "UsageStats{" +
                    "launches=" + launches +
                    '}';
        }
    }
}
