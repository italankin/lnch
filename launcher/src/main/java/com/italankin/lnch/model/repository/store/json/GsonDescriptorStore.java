package com.italankin.lnch.model.repository.store.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.store.DescriptorStore;
import timber.log.Timber;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GsonDescriptorStore implements DescriptorStore {

    private static final Type TYPE = TypeToken.getParameterized(List.class, Descriptor.class).getType();

    private final Gson gson;

    public GsonDescriptorStore(GsonBuilder gsonBuilder) {
        gson = gsonBuilder.create();
    }

    @Override
    public List<Descriptor> read(InputStream in) {
        try (InputStreamReader input = new InputStreamReader(in)) {
            List<Descriptor> descriptors = gson.fromJson(input, TYPE);
            List<Descriptor> notNullDescriptors = new ArrayList<>(descriptors.size());
            for (Descriptor descriptor : descriptors) {
                if (descriptor != null) {
                    notNullDescriptors.add(descriptor);
                }
            }
            return notNullDescriptors;
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
