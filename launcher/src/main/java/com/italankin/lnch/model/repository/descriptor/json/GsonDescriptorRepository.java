package com.italankin.lnch.model.repository.descriptor.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;

import timber.log.Timber;

public class GsonDescriptorRepository implements DescriptorRepository {
    private final Gson gson;

    public GsonDescriptorRepository(GsonBuilder gsonBuilder) {
        gson = gsonBuilder
                .registerTypeAdapter(Descriptor.class, new DescriptorConverter())
                .create();
    }

    @Override
    public List<Descriptor> read(File packagesFile) {
        try {
            return gson.fromJson(new FileReader(packagesFile), getType());
        } catch (Exception e) {
            Timber.e(e, "read:");
            return null;
        }
    }

    @Override
    public void write(File packagesFile, List<Descriptor> items) {
        try {
            String json = gson.toJson(items, getType());
            FileWriter fileWriter = new FileWriter(packagesFile);
            fileWriter.write(json);
            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("Cannot write file=" + packagesFile, e);
        }
    }

    private Type getType() {
        return new TypeToken<List<Descriptor>>() {
        }.getType();
    }
}
