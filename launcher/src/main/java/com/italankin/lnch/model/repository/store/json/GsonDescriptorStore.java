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

    private static final Type TYPE = TypeToken.getParameterized(List.class, Descriptor.class).getType();

    private final Gson gson;

    public GsonDescriptorStore(GsonBuilder gsonBuilder) {
        gson = gsonBuilder.create();
    }

    @Override
    public List<Descriptor> read(InputStream in) {
        try (InputStreamReader input = new InputStreamReader(in)) {
            return gson.fromJson(input, TYPE);
        } catch (Exception e) {
            Timber.e(e, "read:");
            return null;
        }
    }

    @Override
    public void write(OutputStream out, List<Descriptor> items) {
        try (OutputStreamWriter output = new OutputStreamWriter(out)) {
            String json = gson.toJson(items, TYPE);
            output.write(json);
        } catch (Exception e) {
            throw new RuntimeException("cannot write json", e);
        }
    }
}
