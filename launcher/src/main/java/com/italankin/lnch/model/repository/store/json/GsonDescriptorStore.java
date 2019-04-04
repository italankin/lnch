package com.italankin.lnch.model.repository.store.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.store.DescriptorStore;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;

import timber.log.Timber;

public class GsonDescriptorStore implements DescriptorStore {
    private final Gson gson;

    public GsonDescriptorStore(GsonBuilder gsonBuilder) {
        gson = gsonBuilder
                .registerTypeAdapter(Descriptor.class, new DescriptorJsonConverter())
                .create();
    }

    @Override
    public List<Descriptor> read(InputStream in) {
        try (InputStream input = in) {
            return gson.fromJson(new InputStreamReader(input), getType());
        } catch (Exception e) {
            Timber.e(e, "read:");
            return null;
        }
    }

    @Override
    public void write(OutputStream out, List<Descriptor> items) {
        try (OutputStream output = out) {
            String json = gson.toJson(items, getType());
            OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write(json);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Cannot write json", e);
        }
    }

    private Type getType() {
        return new TypeToken<List<Descriptor>>() {
        }.getType();
    }
}
